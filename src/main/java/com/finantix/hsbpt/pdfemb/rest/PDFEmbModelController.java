package com.finantix.hsbpt.pdfemb.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * The Class PDFEmbModelController.
 */
@ConfigurationProperties
@RestController
public class PDFEmbModelController {

    /** The Constant APPLICATION_PROPERTIES. */
    private static final String APPLICATION_PROPERTIES = "application.properties";

    /** The Constant DEFAULT_BUFFER_SIZE. */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(PDFEmbModelController.class.getName());

    /** The delay. */
    @Value("${delay}")
    private int delay;

    /** The is fail execution. */
    @Value("${isFailureExecution}")
    private boolean isFailExecution;

    /**
     * Gr link.
     *
     * @param documentId
     *            the document id
     * @param response
     *            the response
     * @param request
     *            the request
     * @return the streaming response body
     * @throws Exception
     *             the exception
     */
    @ResponseBody
    @PostMapping(RestConstants.GR_LINK)
    public StreamingResponseBody grLink(@NotNull @RequestParam("environment") String documentId, HttpServletResponse response, HttpServletRequest request)
            throws Exception {

        if (request.getInputStream().available() == 0) {
            throw new Exception("Input file not found!");
        }

        // Load property files
        loadProperties();

        // Delay for the response in order to simulate failure scenarios
        waitForMeCase();

        // Delay for the response in order to simulate failure scenarios
        forceExceptionCase();

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gr.PDF\"");
        return outputStream -> {
            StreamUtils.copy(request.getInputStream(), outputStream);
        };

    }

    /**
     * Load properties.
     */
    private void loadProperties() {
        String appPath = System.getProperty("user.dir") + File.separatorChar + APPLICATION_PROPERTIES;
        LOG.debug("Path for configuration : {}", appPath);
        try (InputStream input = new FileInputStream(appPath)) {
            Properties prop = new Properties();
            prop.load(input);
            if (prop.getProperty("delay") != null) {
                delay = Integer.valueOf(prop.getProperty("delay"));
            }
            if (prop.getProperty("isFailureExecution") != null) {
                isFailExecution = Boolean.valueOf(prop.getProperty("isFailureExecution"));
            }
        } catch (IOException e) {
            LOG.error("Error retrieving properties from {} {}", APPLICATION_PROPERTIES, e);
        }
    }

    /**
     * Force exception case.
     *
     * @throws Exception
     *             the exception
     */
    private void forceExceptionCase() throws Exception {
        if (isFailExecution) {
            LOG.info("Force Exception");
            throw new Exception("Failure PDF Embedded scenario");
        }
    }

    /**
     * Wait for me case.
     *
     * @throws InterruptedException
     *             the interrupted exception
     */
    public void waitForMeCase() throws InterruptedException {
        try {
            LOG.info("Sleep {} milliseconds", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new InterruptedException("Error ");
        }
    }

}