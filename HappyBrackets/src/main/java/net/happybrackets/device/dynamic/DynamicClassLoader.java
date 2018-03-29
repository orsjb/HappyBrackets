/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.device.dynamic;


public class DynamicClassLoader extends ClassLoader {
	
	ClassLoader parent;
	
	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
		this.parent = parent;
	}

	public Class<?> createNewClass(byte[] classData) throws ClassFormatError {
//	        System.out.println("Size of class data received = " + classData.length + "(" + classData[0] + "," + classData[1] + "," + classData[2] + ")");
	        return defineClass(null, classData, 0, classData.length);
	}

}
