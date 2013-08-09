/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Web Questionnaires 2
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */
package eionet.webq.web.controller;

import eionet.webq.dto.UploadForm;
import eionet.webq.dto.UploadedXmlFile;
import eionet.webq.dto.XmlSaveResult;
import eionet.webq.service.ConversionService;
import eionet.webq.service.UploadedXmlFileService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Base controller for front page actions.
 *
 * @author Enriko Käsper
 */
@Controller
@RequestMapping("/")
public class BaseController {
    /**
     * File uploadedXmlFileService for user uploaded files.
     */
    @Autowired
    private UploadedXmlFileService uploadedXmlFileService;
    /**
     * File conversion service.
     */
    @Autowired
    private ConversionService conversionService;

    /**
     * Action to be performed on http GET method and path '/'.
     *
     * @param model holder for model attributes
     * @return view name
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome(Model model) {
        model.addAttribute("uploadedFiles", allFilesWithConversions());
        String uploadForm = "uploadForm";
        if (!model.containsAttribute(uploadForm)) {
            model.addAttribute(uploadForm, new UploadForm());
        }
        return "index";
    }

    /**
     * Upload action.
     * @param uploadForm represents form used in UI, {@link UploadForm#uploadedXmlFile} will be converted from
     *            {@link org.springframework.web.multipart.MultipartFile}
     * @param result binding result, contains validation errors
     * @param model holder for model attributes
     * @return view name
     */
    @RequestMapping(value = "/uploadXml", method = RequestMethod.POST)
    public String upload(@Valid @ModelAttribute UploadForm uploadForm, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            UploadedXmlFile file = uploadForm.getUploadedXmlFile();
            uploadedXmlFileService.save(file);
            model.addAttribute("message", "File '" + file.getName() + "' uploaded successfully");
        }
        return welcome(model);
    }

    /**
     * Download uploaded file action.
     *
     * @param fileId requested file id
     * @param response http response to write file
     */
    @RequestMapping(value = "/download")
    public void downloadFile(@RequestParam int fileId, HttpServletResponse response) {
        UploadedXmlFile file = uploadedXmlFileService.getById(fileId);
        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        response.addHeader("Content-Disposition", "attachment;filename=" + file.getName());
        writeToResponse(response, file.getContent());
    }

    /**
     * Update file content action. The action is called from XForms and it returns XML formatted result.
     *
     * @param fileId file id to update
     * @param request current request
     * @return response as application/xml generated by {@link org.springframework.oxm.jaxb.Jaxb2Marshaller}
     */
    @RequestMapping(value = "/saveXml", method = RequestMethod.POST)
    @ResponseBody
    public XmlSaveResult saveXml(@RequestParam int fileId, HttpServletRequest request) {
        UploadedXmlFile file = new UploadedXmlFile();
        XmlSaveResult saveResult = null;
        InputStream input = null;
        try {
            input = request.getInputStream();
            byte[] fileContent = IOUtils.toByteArray(input);
            file.setContent(fileContent);
            file.setSizeInBytes(fileContent.length);
            file.setId(fileId);
            uploadedXmlFileService.updateContent(file);
            saveResult = XmlSaveResult.valueOfSuccess();
        } catch (Exception e) {
            saveResult = XmlSaveResult.valueOfError(e.toString());
        } finally {
            IOUtils.closeQuietly(input);
        }
        return saveResult;
    }

    /**
     * Performs conversion of specified {@link UploadedXmlFile} to specific format.
     * Format is defined by conversionId.
     * @param fileId file id, which will be loaded and converted
     * @param conversionId id of conversion to be used
     * @param response object where conversion result will be written
     */
    @RequestMapping("convert")
    public void convertXmlFile(@RequestParam int fileId, @RequestParam int conversionId, HttpServletResponse response) {
        UploadedXmlFile fileContent = uploadedXmlFileService.getById(fileId);
        writeToResponse(response, conversionService.convert(fileContent, conversionId));
    }

    /**
     * Loads and sets conversions for files uploaded by user.
     *
     * @return all uploaded files with available conversions set.
     */
    private Collection<UploadedXmlFile> allFilesWithConversions() {
        Collection<UploadedXmlFile> uploadedXmlFiles = uploadedXmlFileService.allUploadedFiles();
        for (UploadedXmlFile uploadedXmlFile : uploadedXmlFiles) {
            uploadedXmlFile.setAvailableConversions(conversionService.conversionsFor(uploadedXmlFile.getXmlSchema()));
        }
        return uploadedXmlFiles;
    }

    /**
     * Writes specified content to http response.
     * @param response http response
     * @param data content to be written to response
     */
    private void writeToResponse(HttpServletResponse response, byte[] data) {
        ServletOutputStream output = null;
        try {
            response.setContentLength(data.length);

            output = response.getOutputStream();
            IOUtils.write(data, output);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write response", e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }
}
