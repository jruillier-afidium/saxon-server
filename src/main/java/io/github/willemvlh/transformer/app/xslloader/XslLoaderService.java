package io.github.willemvlh.transformer.app.xslloader;

import io.github.willemvlh.transformer.app.ServerOptions;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltExecutable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.xml.transform.stream.StreamSource;
import java.io.File;

@Service
public class XslLoaderService {

    private final ServerOptions options;

    @Autowired
    public XslLoaderService(ServerOptions options) {
        this.options = options;
    }

    @Cacheable("xsl")
    public XsltExecutable getXsltExecutableFromFilePath(String xslServerPath) throws SaxonApiException {
        File xslFile = new File(this.options.getXslRootPath(), xslServerPath);
        Processor saxonProcessor = new Processor(false);
        return saxonProcessor.newXsltCompiler().compile(new StreamSource(xslFile));
    }

}
