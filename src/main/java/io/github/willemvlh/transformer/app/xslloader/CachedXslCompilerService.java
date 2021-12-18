package io.github.willemvlh.transformer.app.xslloader;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.xml.transform.stream.StreamSource;
import java.io.File;

@Service
public class CachedXslCompilerService {

    private final Logger logger = LoggerFactory.getLogger(CachedXslCompilerService.class);

    private final FileLoaderService fileLoaderService;

    @Autowired
    public CachedXslCompilerService(FileLoaderService fileLoaderService) {
        this.fileLoaderService = fileLoaderService;
    }

    @Cacheable("xsl")
    public XsltLoadResult loadCacheableXsltExecutableFromFilePath(String xslServerPath) throws SaxonApiException {
        File xslFile = this.fileLoaderService.getXslFile(xslServerPath);
        logger.info("Loading XsltExecutable from " + xslFile.getAbsolutePath());
        Processor saxonProcessor = new Processor(false);
        XsltExecutable xsltExecutable = saxonProcessor.newXsltCompiler().compile(new StreamSource(xslFile));
        return new XsltLoadResult(xsltExecutable, xslFile.lastModified());
    }

    @CacheEvict(value = "xsl", key = "#xslServerPath")
    public void evictCache(String xslServerPath) {
    }
}
