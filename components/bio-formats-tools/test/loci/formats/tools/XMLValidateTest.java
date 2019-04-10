/*
 * #%L
 * Bio-Formats command line tools for reading and converting files
 * %%
 * Copyright (C) 2019 Open Microscopy Environment:
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

package loci.formats.tools;

import loci.formats.tools.XMLValidate;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.assertEquals;

/**
 * Tests the functionality of XMLValidate
 */
public class XMLValidateTest {
  @DataProvider(name = "defaults")
  public Object[][] createDefaults() {
    return new Object[][] {
        {"", false},
        {null, false},
    };
  }

  @DataProvider(name = "individual_files")
  public Object[][] createFiles() {
    return new Object[][] {
        {"minimum-specification.ome.xml", true},
        {"invalid.ome.xml", false},
        {"INVALID2.ome.xml", false},
    };
  }

  @Test(dataProvider = "defaults")
  public void testDefaults(String filename, boolean result) throws Exception {
    String[] args =  new String[] {filename};
    assertEquals(XMLValidate.validate(args), new boolean[] {result});
  }

  @Test(dataProvider = "individual_files")
  public void testValidateSingleFile(String filename, boolean result) throws Exception {
    String[] args =  new String[] {
      this.getClass().getResource(filename).getPath(),
    };
    assertEquals(XMLValidate.validate(args), new boolean[] {result});
  }

}
