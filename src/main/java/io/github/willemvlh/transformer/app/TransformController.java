package io.github.willemvlh.transformer.app;

import io.github.willemvlh.transformer.app.xslloader.XslLoaderService;
import io.github.willemvlh.transformer.saxon.actors.ActorType;
import io.github.willemvlh.transformer.saxon.actors.SaxonActor;
import io.github.willemvlh.transformer.saxon.actors.SaxonActorBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

@RestController
class TransformController {

    private final Processor processor;
    private final ServerOptions options;
    private final XslLoaderService xslLoaderService;

    @Autowired
    public TransformController(Processor processor, ServerOptions options, XslLoaderService xslLoaderService) {
        this.processor = processor;
        this.options = options;
        this.xslLoaderService = xslLoaderService;
    }

    @PostMapping(path = {"/query", "/transform"})
    public void doTransform(
            @RequestPart(name = "xml", required = false) Part xml, //use Part instead of MultipartFile so that we can also send strings
            @RequestPart(name = "xsl", required = false) Part xsl,
            @RequestParam(name = "output", required = false) String output,
            @RequestParam(name = "parameters", required = false) String parameters,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

        // Parse parameters
        Map<String, String> params = new ParameterParser().parseString(parameters);
        Map<String, String> serParams = new ParameterParser().parseString(output);

        // Prepare Saxon transformer
        SaxonActorBuilder builder = getBuilder(request.getRequestURI());
        SaxonActor tf = builder
                .setProcessor(processor)
                .setTimeout(options.getTransformationTimeoutMs())
                .setParameters(params)
                .setSerializationProperties(serParams)
                .build();

        // Set content type
        response.setContentType("method=json".equals(output) ? "application/json" : "application/xml");

        // Prepare streams
        Optional<InputStream> xmlStr = Optional.ofNullable(xml).flatMap(this::getInputStream);
        InputStream xslStr = Optional.ofNullable(xsl).flatMap(this::getInputStream)
                .orElseThrow(() -> new InvalidRequestException("No XSL supplied"));
        OutputStream os = new BufferedOutputStream(response.getOutputStream());

        // Run transformation
        if (xmlStr.isPresent()) {
            tf.act(new BufferedInputStream(xmlStr.get()), xslStr, os);
        } else {
            tf.act(xslStr, os);
        }
    }

    @PostMapping(path = {"/transformWithServerStylesheet"})
    public void doTransformWithServerStylesheet(
            @RequestParam(name = "xslServerPath") String xslServerPath,
            @RequestPart(name = "xml") Part xml, //use Part instead of MultipartFile so that we can also send strings
            @RequestParam(name = "output", required = false) String output,
            @RequestParam(name = "parameters", required = false) String parameters,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

        // Parse parameters
        Map<String, String> params = new ParameterParser().parseString(parameters);
        Map<String, String> serParams = new ParameterParser().parseString(output);

        // Prepare Saxon transformer
        SaxonActorBuilder builder = getBuilder(request.getRequestURI());
        SaxonActor tf = builder
                .setProcessor(processor)
                .setTimeout(options.getTransformationTimeoutMs())
                .setParameters(params)
                .setSerializationProperties(serParams)
                .build();

        // Set content type
        response.setContentType("method=json".equals(output) ? "application/json" : "application/xml");

        // Prepare transformer
        Processor saxonProcessor = new Processor(false);
        XsltExecutable xsltExecutable = this.xslLoaderService.getXsltExecutableFromFilePath(xslServerPath);
        XsltTransformer xsltTransformer = xsltExecutable.load();
        xsltTransformer.setSource(new StreamSource(xml.getInputStream()));
        xsltTransformer.setDestination(saxonProcessor.newSerializer(response.getOutputStream()));

        // Run transformer
        xsltTransformer.transform();
    }

    private Optional<InputStream> getInputStream(Part p) {
        try {
            String contentType = p.getContentType();
            if ("application/gzip".equalsIgnoreCase(contentType)) {
                return Optional.of(getZippedStreamFromPart(new BufferedInputStream(p.getInputStream())));
            }
            return Optional.ofNullable(p.getInputStream());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private InputStream getZippedStreamFromPart(InputStream input) {
        try {
            GZIPInputStream s = new GZIPInputStream(input);
            ZippedStreamReader r = new ZippedStreamReader();
            return r.unzipStream(s);
        } catch (IOException e) {
            throw new InvalidRequestException(e);
        }
    }

    private SaxonActorBuilder getBuilder(String requestURI) {
        switch (requestURI) {
            case "/query":
                return ActorType.QUERY.getBuilder();
            default:
                return ActorType.TRANSFORM.getBuilder();
        }
    }
}

