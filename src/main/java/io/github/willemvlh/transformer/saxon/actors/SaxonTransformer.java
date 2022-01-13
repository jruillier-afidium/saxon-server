package io.github.willemvlh.transformer.saxon.actors;

import io.github.willemvlh.transformer.app.xslloader.XslLoaderService;
import io.github.willemvlh.transformer.saxon.SaxonMessageListener;
import io.github.willemvlh.transformer.saxon.SerializationProps;
import io.github.willemvlh.transformer.saxon.TransformationException;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SaxonTransformer extends SaxonActor {

    private final ArrayList<StaticError> errorList = new ArrayList<>();

    private XslLoaderService xslLoaderService;

    private Xslt30Transformer transformer;

    public List<StaticError> getErrorList() {
        return errorList;
    }

    @Override
    public SerializationProps act(XdmValue input, InputStream stylesheet, OutputStream output) throws TransformationException {
        transformer = newTransformer(stylesheet);
        return this.doAct(input, output);
    }

    @Override
    protected SerializationProps act(XdmValue input, String stylesheetServerPath, OutputStream output) throws TransformationException {
        transformer = newTransformer(stylesheetServerPath);
        return this.doAct(input, output);
    }

    protected SerializationProps doAct(XdmValue input, OutputStream output) throws TransformationException {
        Serializer s = newSerializer(output);
        try {
            transformer.setStylesheetParameters(this.getParameters());
            if (input.isEmpty()) {
                //no input, use default template "xsl:initial-template"
                transformer.callTemplate(null, s);
            } else {
                //apply templates on context item
                transformer.setGlobalContextItem(input.itemAt(0));
                transformer.applyTemplates(input, s);
            }
            return getSerializationProperties(s);
        } catch (SaxonApiException e) {
            SaxonMessageListener listener = (SaxonMessageListener) transformer.getMessageListener2();
            String msg = listener.errorString != null ? listener.errorString : e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new TransformationException(msg, e);
        }
    }

    @Override
    protected Serializer newSerializer(OutputStream os) {
        Serializer s = transformer.newSerializer(os);
        SerializationProperties props = new SerializationProperties();
        getSerializationParameters().forEach(props::setProperty);
        s.setOutputProperties(s.getSerializationProperties().combineWith(props));
        return s;
    }

    @Override
    public void setXslLoaderService(XslLoaderService xslLoaderService) {
        this.xslLoaderService = xslLoaderService;
    }

    private Xslt30Transformer newTransformer(InputStream stylesheet) throws TransformationException {
        Processor p = getProcessor();
        XsltCompiler c = newXsltCompiler(p);
        try {
            XsltExecutable e = c.compile(newSAXSource(stylesheet));
            return loadAndPrepareTransformer(e);
        } catch (SaxonApiException e) {
            throw this.processAndGetXslCompilationError(e);
        }
    }

    private Xslt30Transformer newTransformer(String stylesheetServerPath) throws TransformationException {
        try {
            Processor p = getProcessor();
            XsltExecutable e = this.xslLoaderService.getXsltExecutableFromFilePath(
                    stylesheetServerPath,
                    newXsltCompiler(p),
                    this.configurationFactory
            );
            return loadAndPrepareTransformer(e);
        } catch (SaxonApiException e) {
            throw this.processAndGetXslCompilationError(e);
        }
    }

    private XsltCompiler newXsltCompiler(Processor p) {
        XsltCompiler c = p.newXsltCompiler();
        c.setErrorList(this.getErrorList());
        return c;
    }

    private Xslt30Transformer loadAndPrepareTransformer(XsltExecutable e) {
        Xslt30Transformer xf = e.load30();
        xf.setMessageListener(new SaxonMessageListener());
        return xf;
    }

    private TransformationException processAndGetXslCompilationError(SaxonApiException e) {
        if (this.getErrorList().size() > 0) {
            StaticError error = this.getErrorList().get(0);
            String message;
            if (error instanceof XmlProcessingError && ((XmlProcessingError) error).getCause() != null) {
                message = ((XmlProcessingError) error).getCause().getMessage();
                //will usually contain a parsing error
            } else {
                message = error.getMessage();
            }
            if (error.getLocation() != null) {
                message = message + " (line " + error.getLineNumber() + ", col " + error.getColumnNumber() + ")";
            }
            return new TransformationException("Compilation error: " + message);
        }
        return new TransformationException(e.getMessage());
    }

}
