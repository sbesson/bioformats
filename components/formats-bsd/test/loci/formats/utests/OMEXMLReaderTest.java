/*
 * #%L
 * BSD implementations of Bio-Formats readers and writers
 * %%
 * Copyright (C) 2005 - 2017 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.formats.utests;

import java.io.IOException;
import java.io.File;

import loci.formats.FormatException;
import loci.formats.in.OMEXMLReader;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.services.OMEXMLService;

import loci.common.services.ServiceFactory;
import loci.common.services.ServiceException;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;


/**
 */
public class OMEXMLReaderTest {

  private OMEXMLReader reader = new OMEXMLReader();
  private OMEXMLService service;
  private MetadataRetrieve m;
  
  private String getId(String xmlPath) {
    File xmlFile = new File(getClass().getClassLoader().getResource("resources/" + xmlPath).getFile());
    return xmlFile.getAbsolutePath();
  }

  @BeforeMethod
  public void setUp() throws Exception {
    reader = new OMEXMLReader();
    ServiceFactory sf = new ServiceFactory();
    service = sf.getInstance(OMEXMLService.class);
    reader.setMetadataStore(service.createOMEXMLMetadata());
    reader.setFlattenedResolutions(false);
  }

  @Test
  public void testMinimalSpecification() throws Exception {
    reader.setId(getId("minimum-specification.ome.xml"));
    m = service.asRetrieve(reader.getMetadataStore());
    assertTrue(service.validateOMEXML(service.getOMEXML(m)));
    assertEquals(m.getImageCount(), 1);
    assertEquals(m.getImageID(0), "Image:0");
    assertEquals(m.getImageName(0), "Name92");
    assertEquals(m.getPixelsSizeX(0), 2);
    assertEquals(m.getPixelsSizeY(0), 2);
    assertEquals(m.getPixelsSizeZ(0), 2);
    assertEquals(m.getPixelsSizeC(0), 2);
    assertEquals(m.getPixelsSizeT(0), 2);
  }

  @Test
  public void testDuplicateImageIDs() throws Exception {
    reader.setId(getId("duplicate_image_ids.ome.xml"));
    m = service.asRetrieve(reader.getMetadataStore());
    assertTrue(service.validateOMEXML(service.getOMEXML(m)));
    assertEquals(m.getImageCount(), 2);
    assertEquals(m.getImageID(0), "Image:0");
    assertEquals(m.getImageName(0), "Name91");
    assertEquals(m.getPixelsID(0), "Pixels:0");
    assertEquals(m.getPixelsSizeX(0), 2);
    assertEquals(m.getPixelsSizeY(0), 2);
    assertEquals(m.getPixelsSizeZ(0), 2);
    assertEquals(m.getPixelsSizeC(0), 2);
    assertEquals(m.getPixelsSizeT(0), 2);
    assertEquals(m.getImageID(1), "Image:1");
    assertEquals(m.getImageName(1), "Name92");
    assertEquals(m.getPixelsID(1), "Pixels:1");
    assertEquals(m.getPixelsSizeX(1), 2);
    assertEquals(m.getPixelsSizeY(1), 2);
    assertEquals(m.getPixelsSizeZ(1), 2);
    assertEquals(m.getPixelsSizeC(1), 2);
    assertEquals(m.getPixelsSizeT(1), 2);
  }

  @Test
  public void testProblemPlate() throws Exception {
    reader.setId(getId("problem-plate.ome.xml"));
    m = service.asRetrieve(reader.getMetadataStore());
    // assertTrue(service.validateOMEXML(service.getOMEXML(m)));
    assertEquals(m.getPlateCount(), 1);
    assertEquals(m.getImageCount(), 9);
    assertEquals(m.getWellCount(0), 9);
    assertEquals(m.getWellID(0, 0), "Well:1");
    assertEquals(m.getWellSampleID(0, 0, 0), "WellSample:1");
    assertEquals(m.getWellSampleImageRef(0, 0, 0), "Image:1");
    assertEquals(m.getWellSampleIndex(0, 0, 0), 1);
    assertEquals(m.getWellID(0, 1), "Well:2");
    assertEquals(m.getWellSampleID(0, 1, 0), "WellSample:2");
    assertEquals(m.getWellSampleImageRef(0, 1, 0), "Image:2");
    assertEquals(m.getWellSampleIndex(0, 1, 0), 2);
    assertEquals(m.getWellID(0, 2), "Well:3");
    assertEquals(m.getWellSampleID(0, 2, 0), "WellSample:3");
    assertEquals(m.getWellSampleImageRef(0, 2, 0), "Image:3");
    assertEquals(m.getWellSampleIndex(0, 2, 0), 3);
    assertEquals(m.getWellID(0, 3), "Well:4");
    assertEquals(m.getWellSampleID(0, 3, 0), "WellSample:4");
    assertEquals(m.getWellSampleImageRef(0, 3, 0), "Image:4");
    assertEquals(m.getWellSampleIndex(0, 3, 0), 4);
    assertEquals(m.getWellID(0, 4), "Well:5");
    assertEquals(m.getWellSampleID(0, 4, 0), "WellSample:5");
    assertEquals(m.getWellSampleImageRef(0, 4, 0), "Image:5");
    assertEquals(m.getWellSampleIndex(0, 4, 0), 5);
    assertEquals(m.getWellID(0, 5), "Well:6");
    assertEquals(m.getWellSampleID(0, 5, 0), "WellSample:6");
    assertEquals(m.getWellSampleImageRef(0, 5, 0), "Image:6");
    assertEquals(m.getWellSampleIndex(0, 5, 0), 6);
    assertEquals(m.getWellID(0, 6), "Well:7");
    assertEquals(m.getWellSampleID(0, 6, 0), "WellSample:7");
    assertEquals(m.getWellSampleImageRef(0, 6, 0), "Image:7");
    assertEquals(m.getWellSampleIndex(0, 6, 0), 7);
    assertEquals(m.getWellID(0, 7), "Well:8");
    assertEquals(m.getWellSampleID(0, 7, 0), "WellSample:8");
    assertEquals(m.getWellSampleImageRef(0, 7, 0), "Image:8");
    assertEquals(m.getWellSampleIndex(0, 7, 0), 8);
    assertEquals(m.getWellID(0, 8), "Well:9");
    assertEquals(m.getWellSampleID(0, 8, 0), "WellSample:9");
    assertEquals(m.getWellSampleImageRef(0, 8, 0), "Image:8");
    assertEquals(m.getWellSampleIndex(0, 8, 0), 9);
    assertEquals(m.getImageID(0), "Image:0");
    assertEquals(m.getImageID(1), "Image:1");
    assertEquals(m.getImageID(2), "Image:2");
    assertEquals(m.getImageID(3), "Image:3");
    assertEquals(m.getImageID(4), "Image:4");
    assertEquals(m.getImageID(5), "Image:5");
    assertEquals(m.getImageID(6), "Image:6");
    assertEquals(m.getImageID(7), "Image:7");
    assertEquals(m.getImageID(8), "Image:8");
  }

  @AfterMethod
  public void tearDown() throws IOException {
    reader.close();
  }
}