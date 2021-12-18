package io.github.willemvlh.transformer.app.xslloader;

import net.sf.saxon.s9api.XsltExecutable;

public class XsltLoadResult {
    public XsltExecutable xsltExecutable;
    public long lastModified;

    public XsltLoadResult(XsltExecutable xsltExecutable, long lastModified) {
        this.xsltExecutable = xsltExecutable;
        this.lastModified = lastModified;
    }
}
