package io.github.willemvlh.transformer.app.xslloader;

import io.github.willemvlh.transformer.saxon.config.SaxonConfigurationFactory;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class XslLoaderService {

    public static final String ATTEMPT_DATE_FORMAT = "yyyy-MM-dd'T'hh-mm";

    private final Logger logger = LoggerFactory.getLogger(XslLoaderService.class);

    private final FileLoaderService fileLoaderService;
    private final CachedXslCompilerService xslCompilerService;

    private final Map<String, String> loadAttempts = new ConcurrentHashMap<>();

    @Autowired
    public XslLoaderService(FileLoaderService fileLoaderService, CachedXslCompilerService xslCompilerService) {
        this.fileLoaderService = fileLoaderService;
        this.xslCompilerService = xslCompilerService;
    }

    public XsltExecutable getXsltExecutableFromFilePath(
            String xslServerPath,
            XsltCompiler xsltCompiler,
            SaxonConfigurationFactory configurationFactory) throws SaxonApiException {

        XsltLoadResult xsltLoadResult = this.xslCompilerService.loadCacheableXsltExecutableFromFilePath(
                xslServerPath,
                xsltCompiler,
                configurationFactory
        );

        String currentAttemptDate = this.getCurrentAttemptDate();
        String previousAttemptDate = loadAttempts.put(xslServerPath, currentAttemptDate);

        if (previousAttemptDate == null) {
            logger.info("XSL was loaded for the first time : " + xslServerPath);
            return xsltLoadResult.xsltExecutable;
        }

        if (currentAttemptDate.compareTo(previousAttemptDate) == 0) {
            logger.info("XSL served in the same minute : " + xslServerPath);
            return xsltLoadResult.xsltExecutable;
        }

        if (xsltLoadResult.lastModified < this.fileLoaderService.getXslFile(xslServerPath).lastModified()) {
            logger.info("Evicting XSL from cache, file has changed : " + xslServerPath);
            this.xslCompilerService.evictCache(xslServerPath);
        } else {
            logger.info("XSL did not change : " + xslServerPath);
        }

        return this.xslCompilerService.loadCacheableXsltExecutableFromFilePath(
                xslServerPath,
                xsltCompiler,
                configurationFactory).xsltExecutable;
    }

    private String getCurrentAttemptDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(ATTEMPT_DATE_FORMAT);
        return dateFormat.format(new Date());
    }

}
