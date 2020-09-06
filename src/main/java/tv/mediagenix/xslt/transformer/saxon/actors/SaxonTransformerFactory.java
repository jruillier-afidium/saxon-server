package tv.mediagenix.xslt.transformer.saxon.actors;

import tv.mediagenix.xslt.transformer.saxon.TransformationException;

import java.io.File;

public class SaxonTransformerFactory extends SaxonActorFactory {

    @Override
    public SaxonActor newActor(boolean insecure) {
        return new SaxonTransformer(insecure);
    }

    @Override
    public SaxonActor newActorWithConfig(File config) throws TransformationException {
        return new SaxonTransformer(config);
    }
}
