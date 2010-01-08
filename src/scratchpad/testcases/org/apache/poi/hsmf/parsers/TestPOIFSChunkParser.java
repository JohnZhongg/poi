/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hsmf.parsers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

/**
 * Tests to verify that the chunk parser works properly
 */
public final class TestPOIFSChunkParser extends TestCase {
   private POIDataSamples samples;

	public TestPOIFSChunkParser() throws IOException {
        samples = POIDataSamples.getHSMFInstance();
	}
	
   public void testFindsRecips() throws IOException {
      
   }
   
	public void testFindsAttachments() throws IOException {
	   POIFSFileSystem with = new POIFSFileSystem(
	         new FileInputStream(samples.getFile("attachment_test_msg.msg"))
	   );
      POIFSFileSystem without = new POIFSFileSystem(
            new FileInputStream(samples.getFile("quick.msg"))
      );
      AttachmentChunks attachment;
      
      
      // Check raw details on the one with
      with.getRoot().getEntry("__attach_version1.0_#00000000");
      with.getRoot().getEntry("__attach_version1.0_#00000001");
      POIFSChunkParser.parse(with.getRoot());
      
      ChunkGroup[] groups = POIFSChunkParser.parse(with.getRoot());
      assertEquals(5, groups.length);
      assertTrue(groups[0] instanceof Chunks);
      assertTrue(groups[1] instanceof RecipientChunks);
      assertTrue(groups[2] instanceof AttachmentChunks);
      assertTrue(groups[3] instanceof AttachmentChunks);
      assertTrue(groups[4] instanceof NameIdChunks);
      
      attachment = (AttachmentChunks)groups[2];
      assertEquals("TEST-U~1.DOC", attachment.attachFileName.toString());
      assertEquals("test-unicode.doc", attachment.attachLongFileName.toString());
      assertEquals(24064, attachment.attachData.getValue().length);
      
      attachment = (AttachmentChunks)groups[3];
      assertEquals("pj1.txt", attachment.attachFileName.toString());
      assertEquals("pj1.txt", attachment.attachLongFileName.toString());
      assertEquals(89, attachment.attachData.getValue().length);
	   
      
      // Check raw details on one without
      try {
         without.getRoot().getEntry("__attach_version1.0_#00000000");
         fail();
      } catch(FileNotFoundException e) {}
      try {
         without.getRoot().getEntry("__attach_version1.0_#00000001");
         fail();
      } catch(FileNotFoundException e) {}
      
      
	   // One with, from the top
      MAPIMessage msgWith = new MAPIMessage(with);
      assertEquals(2, msgWith.getAttachmentFiles().length);

      attachment = msgWith.getAttachmentFiles()[0];
      assertEquals("TEST-U~1.DOC", attachment.attachFileName.toString());
      assertEquals("test-unicode.doc", attachment.attachLongFileName.toString());
      assertEquals(24064, attachment.attachData.getValue().length);
      
      attachment = msgWith.getAttachmentFiles()[1];
      assertEquals("pj1.txt", attachment.attachFileName.toString());
      assertEquals("pj1.txt", attachment.attachLongFileName.toString());
      assertEquals(89, attachment.attachData.getValue().length);
      
      // Plus check core details are there
      try {
         assertEquals("'nicolas1.23456@free.fr'", msgWith.getDisplayTo());
         assertEquals("Nicolas1 23456", msgWith.getDisplayFrom());
         assertEquals("test pi\u00e8ce jointe 1", msgWith.getSubject());
      } catch(ChunkNotFoundException e) {
         fail();
      }
	   
      
	   // One without, from the top
      MAPIMessage msgWithout = new MAPIMessage(without);
      
      // No attachments
      assertEquals(0, msgWithout.getAttachmentFiles().length);
      
      // But has core details
      try {
         assertEquals("Kevin Roast", msgWithout.getDisplayTo());
         assertEquals("Kevin Roast", msgWithout.getDisplayFrom());
         assertEquals("Test the content transformer", msgWithout.getSubject());
      } catch(ChunkNotFoundException e) {
         fail();
      }
	}
}
