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
/**
 * 
 */
package org.openimaj.image.processor;

import org.openimaj.image.Image;

/**
 *	A processor that will process an image in a grid-wise fashion returning
 *	a single value for each grid element.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *  @param <T> the pixel type
 *  @param <I> the image type 
 */
public interface GridProcessor<T,I extends Image<T,I>> extends Processor<I>
{
	/**
	 * 	Returns the number of columns in the grid.
	 *  @return the number of columns in the grid.
	 */
	public abstract int getHorizontalGridElements();
	
	/**
	 * 	Returns the number of rows in the grid.
	 *  @return the number of rows in the grid.
	 */
	public abstract int getVerticalGridElements();
	
	/**
	 * 	Process the given grid element (<code>patch</code>) and returns
	 * 	a single pixel value for that element.
	 * 
	 *  @param patch The patch of the grid to process 
	 *  @return A single pixel value
	 */
	public abstract T processGridElement( I patch );
}
