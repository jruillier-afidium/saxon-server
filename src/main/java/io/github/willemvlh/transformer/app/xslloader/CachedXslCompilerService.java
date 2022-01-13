package io.github.willemvlh.transformer.app.xslloader;

import io.github.willemvlh.transformer.saxon.config.SaxonConfigurationFactory;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Service
public class CachedXslCompilerService {

    private final Logger logger = LoggerFactory.getLogger(CachedXslCompilerService.class);

    private final FileLoaderService fileLoaderService;

    @Autowired
    public CachedXslCompilerService(FileLoaderService fileLoaderService) {
        this.fileLoaderService = fileLoaderService;
    }

    @Cacheable(value = "xsl", key = "#xslServerPath")
    public XsltLoadResult loadCacheableXsltExecutableFromFilePath(
            String xslServerPath,
            XsltCompiler xsltCompiler,
            SaxonConfigurationFactory configurationFactory) throws SaxonApiException {
        File xslFile = this.fileLoaderService.getXslFile(xslServerPath);
        logger.info("Compiling XsltExecutable from " + xslFile.getAbsolutePath());
        try {
            XsltExecutable xsltExecutable =
                    xsltCompiler.compile(configurationFactory.newSAXSource(new FileInputStream(xslFile)));
            return new XsltLoadResult(xsltExecutable, xslFile.lastModified());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @CacheEvict(value = "xsl", key = "#xslServerPath")
    public void evictCache(String xslServerPath) {
    }
}
