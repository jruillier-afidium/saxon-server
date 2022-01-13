package io.github.willemvlh.transformer.app.xslloader;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.willemvlh.transformer.app.ServerOptions;

class FileLoaderServiceTest {

	@Test
	void testRelativePath() {
		ServerOptions options = new ServerOptions();
		options.setXslRootPath("/xsl/root/path");
		
		FileLoaderService fileLoaderService = new FileLoaderService(options);
		
		assertEquals("/xsl/root/path/subpath/stylesheet.xsl", 
				fileLoaderService.getXslFile("subpath/stylesheet.xsl").getAbsolutePath());
	}

}
