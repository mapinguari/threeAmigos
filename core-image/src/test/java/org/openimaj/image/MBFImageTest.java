/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.image.colour.ColourSpace;

/**
 * Tests for MBFImage
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MBFImageTest {
	private MBFImage alphaImage;
	private MBFImage testImage;

	/**
	 * Setup test
	 * 
	 * @throws IOException
	 *             if error occurs
	 */
	@Before
	public void setup() throws IOException {
		final InputStream alphaStream = this.getClass().getResourceAsStream("/org/openimaj/image/data/alphaimage.png");
		final InputStream testImageStream = this.getClass().getResourceAsStream("/org/openimaj/image/data/sinaface.jpg");

		alphaImage = ImageUtilities.readMBFAlpha(alphaStream);
		testImage = ImageUtilities.readMBF(testImageStream);
	}

	/**
	 * Test alpha reading and compositing
	 */
	@Test
	public void alphaTest() {
		assertEquals(ColourSpace.RGBA, alphaImage.getColourSpace());
		assertEquals(ColourSpace.RGB, testImage.getColourSpace());
	}

	/**
	 * Test drawing of one image into another over a range of positions
	 */
	@Test
	public void testDrawImage() {
		for (int cs1 = 1; cs1 < 5; cs1++) {
			final MBFImage im1 = new MBFImage(100, 100, cs1);

			for (int cs2 = 1; cs2 < 5; cs2++) {
				final MBFImage im2 = new MBFImage(11, 11, cs2);

				for (int y = -20; y < 120; y++) {
					for (int x = -20; x < 120; x++) {
						im1.drawImage(im2, x, y);
					}
				}
			}
		}
	}
}
