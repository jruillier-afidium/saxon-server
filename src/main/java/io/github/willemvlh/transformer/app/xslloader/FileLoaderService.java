package io.github.willemvlh.transformer.app.xslloader;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.willemvlh.transformer.app.ServerOptions;

@Service
public class FileLoaderService {

	private final ServerOptions options;

	@Autowired
	public FileLoaderService(ServerOptions options) {
		this.options = options;
	}

	File getXslFile(String xslServerPath) {
		// If xslServerPath starts with /, it is supposed to be an absolute path.
		if (xslServerPath.startsWith("/")) {
			xslServerPath = convertToRelativePath(xslServerPath);
		}

		return new File(this.options.getXslRootPath(), xslServerPath);
	}

	/**
	 * Check that the given absolute path contains the configured xslRootPath and
	 * convert it to a relative path.
	 * 
	 * @param xslAbsolutePath the supposed xsl file absolute path.
	 * @return the relative path.
	 * @throws @SecurityException it the absolute path does not includes the
	 *                            configured xslRootPath.
	 */
	String convertToRelativePath(final String xslAbsolutePath) {
		if (xslAbsolutePath.startsWith(this.options.getXslRootPath())) {
			return xslAbsolutePath.replace(this.options.getXslRootPath(), "");
		} else {
			throw new SecurityException("Using absolute path outside of configured xslRootPath is forbidden.");
		}
	}
}
