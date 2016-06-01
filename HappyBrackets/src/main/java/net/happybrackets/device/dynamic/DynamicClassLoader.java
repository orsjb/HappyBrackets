package net.happybrackets.device.dynamic;


public class DynamicClassLoader extends ClassLoader {
	
	ClassLoader parent;
	
	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
		this.parent = parent;
	}

	public Class<?> createNewClass(byte[] classData) {
//	        System.out.println("Size of class data received = " + classData.length + "(" + classData[0] + "," + classData[1] + "," + classData[2] + ")");
	        return defineClass(null, classData, 0, classData.length);
	}

}
