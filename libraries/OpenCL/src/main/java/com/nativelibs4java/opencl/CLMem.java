/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/

package com.nativelibs4java.opencl;
import com.nativelibs4java.opencl.library.OpenCLLibrary;
import com.nativelibs4java.util.EnumValue;
import com.nativelibs4java.util.EnumValues;
import com.ochafik.util.listenable.Pair;
import static com.nativelibs4java.opencl.library.OpenCLLibrary.*;
import com.sun.jna.*;
import com.sun.jna.ptr.*;
import java.nio.*;
import java.util.EnumSet;
import static com.nativelibs4java.opencl.OpenCL4Java.*;
import static com.nativelibs4java.opencl.CLException.*;
import static com.nativelibs4java.util.JNAUtils.*;
import static com.nativelibs4java.util.NIOUtils.*;

/**
 * OpenCL memory object.<br/>
 * Memory objects are categorized into two types: buffer objects, and image objects. <br/>
 * A buffer object stores a one-dimensional collection of elements whereas an image object is used to store a two- or three- dimensional texture, frame-buffer or image.<br/>
 * Elements of a buffer object can be a scalar data type (such as an int, float), vector data type, or a user-defined structure. An image object is used to represent a buffer that can be used as a texture or a frame-buffer. The elements of an image object are selected from a list of predefined image formats. <br/>
 * The minimum number of elements in a memory object is one.<br/>
 * The fundamental differences between a buffer and an image object are:
 * <ul>
 * <li>Elements in a buffer are stored in sequential fashion and can be accessed using a pointer by a kernel executing on a device. Elements of an image are stored in a format that is opaque to the user and cannot be directly accessed using a pointer. Built-in functions are provided by the OpenCL C programming language to allow a kernel to read from or write to an image.</li>
 * <li>For a buffer object, the data is stored in the same format as it is accessed by the kernel, but in the case of an image object the data format used to store the image elements may not be the same as the data format used inside the kernel. Image elements are always a 4- component vector (each component can be a float or signed/unsigned integer) in a kernel. The built-in function to read from an image converts image element from the format it is stored into a 4-component vector. Similarly, the built-in function to write to an image converts the image element from a 4-component vector to the appropriate image format specified such as 4 8-bit elements, for example.</li>
 * </ul>
 *
 * Kernels take memory objects as input, and output to one or more memory objects.
 * @author Olivier Chafik
 */
public abstract class CLMem extends CLAbstractEntity<cl_mem> {

    protected final CLContext context;
    protected final long byteCount;

    CLMem(CLContext context, long byteCount, cl_mem entity) {
        super(entity);
        this.byteCount = byteCount;
        this.context = context;
    }
    public CLContext getContext() {
        return context;
    }
    public long getByteCount() {
        return byteCount;
    }

	/**
	 * Memory Object Usage enum
	 */
	public enum Usage {
		Input(CL_MEM_READ_ONLY, Flags.ReadOnly),
		Output(CL_MEM_READ_WRITE, Flags.WriteOnly),
		InputOutput(CL_MEM_WRITE_ONLY, Flags.ReadWrite);

		private int intFlags;
		private Flags flags;
		Usage(int intFlags, Flags flags) {
			this.intFlags = intFlags;
			this.flags = flags;
		}
		public int getIntFlags() {
			return intFlags;
		}
		public Flags getFlags() {
			return flags;
		}
	}

	public enum Flags {
		/**
		 * This flag specifies that the memory object will be read and written by a kernel. This is the default.
		 */
		@EnumValue(CL_MEM_READ_WRITE)		ReadWrite,
		/**
		 * This flags specifies that the memory object will be written but not read by a kernel.<br/>
		 * Reading from a buffer or image object created with CL_MEM_WRITE_ONLY inside a kernel is undefined.
		 */
		@EnumValue(CL_MEM_WRITE_ONLY)		WriteOnly,
		/**
		 * This flag specifies that the memory object is a read-only memory object when used inside a kernel. <br/>
		 * Writing to a buffer or image object created with CL_MEM_READ_ONLY inside a kernel is undefined.
		 */
		@EnumValue(CL_MEM_READ_ONLY)		ReadOnly,
		/**
		 * This flag is valid only if host_ptr is not NULL. If specified, it indicates that the application wants the OpenCL implementation to use memory referenced by host_ptr as the storage bits for the memory object. <br/>
		 * OpenCL implementations are allowed to cache the buffer contents pointed to by host_ptr in device memory. This cached copy can be used when kernels are executed on a device. <br/>
		 * The result of OpenCL commands that operate on multiple buffer objects created with the same host_ptr or overlapping host regions is considered to be undefined.
		 */
		@EnumValue(CL_MEM_USE_HOST_PTR)		UseHostPtr,
		/**
		 * This flag specifies that the application wants the OpenCL implementation to allocate memory from host accessible memory. <br/>
		 * CL_MEM_ALLOC_HOST_PTR and CL_MEM_USE_HOST_PTR are mutually exclusive.<br/>
		 * CL_MEM_COPY_HOST_PTR: This flag is valid only if host_ptr is not NULL. If specified, it indicates that the application wants the OpenCL implementation to allocate memory for the memory object and copy the data from memory referenced by host_ptr.<br/>
		 * CL_MEM_COPY_HOST_PTR and CL_MEM_USE_HOST_PTR are mutually exclusive.<br/>
		 * CL_MEM_COPY_HOST_PTR can be used with CL_MEM_ALLOC_HOST_PTR to initialize the contents of the cl_mem object allocated using host-accessible (e.g. PCIe) memory.
		 */
		@EnumValue(CL_MEM_ALLOC_HOST_PTR)		AllocHostPtr,
		@EnumValue(CL_MEM_COPY_HOST_PTR)		CopyHostPtr;

		public long getValue() { return EnumValues.getValue(this); }
		public static long getValue(EnumSet<Flags> set) { return EnumValues.getValue(set); }
		public static EnumSet<Flags> getEnumSet(long v) { return EnumValues.getEnumSet(v, Flags.class); }
	}
	public enum ObjectType {
		@EnumValue(CL_MEM_OBJECT_BUFFER) Buffer,
		@EnumValue(CL_MEM_OBJECT_IMAGE2D) Image2D,
		@EnumValue(CL_MEM_OBJECT_IMAGE3D) Image3D;

		public long getValue() { return EnumValues.getValue(this); }
		public static ObjectType getEnum(long v) { return EnumValues.getEnum(v, ObjectType.class); }
	}

	public enum MapFlags {
		@EnumValue(CL_MAP_READ) Read,
		@EnumValue(CL_MAP_WRITE) Write,
		@EnumValue(CL_MAP_READ | CL_MAP_WRITE) ReadWrite;

		public long getValue() { return EnumValues.getValue(this); }
		public static MapFlags getEnum(long v) { return EnumValues.getEnum(v, MapFlags.class); }
	}

    @Override
    protected void clear() {
        error(CL.clReleaseMemObject(get()));
    }
}