/*
 * Authors Name: Jeff Molofee (NeHe)
 *
 * Disclaimer:
 * This program may crash your system or run poorly depending on your
 * hardware.  The program and code contained in this archive was scanned
 * for virii and has passed all test before it was put online.  If you
 * use this code in project of your own, send a shout out to the author!
 *
 * Ported to Android by INsanityDesign:
 *  http://insanitydesign.com/wp/projects/nehe-android-ports/
 */
package com.sformica.benchmark.lesson16

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLUtils
import com.sformica.benchmark.R
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

/**
 * This class is an object representation of
 * a Cube containing the vertex information,
 * texture coordinates, the vertex indices
 * and drawing functionality, which is called
 * by the renderer.
 *
 * @author Savas Ziplies (nea/INsanityDesign)
 */
class Cube {
    /**
     * The buffer holding the vertices
     */
    private val vertexBuffer: FloatBuffer

    /**
     * The buffer holding the texture coordinates
     */
    private val textureBuffer: FloatBuffer

    /**
     * The buffer holding the indices
     */
    private val indexBuffer: ByteBuffer

    /**
     * The buffer holding the normals
     */
    private val normalBuffer: FloatBuffer

    /**
     * Our texture pointer
     */
    private val textures = IntArray(3)

    /**
     * The initial indices definition
     */
    private val indices = byteArrayOf( // Faces definition
        0, 1, 3, 0, 3, 2,  // Face front
        4, 5, 7, 4, 7, 6,  // Face right
        8, 9, 11, 8, 11, 10,  // ...
        12, 13, 15, 12, 15, 14,
        16, 17, 19, 16, 19, 18,
        20, 21, 23, 20, 23, 22
    )

    /**
     * The object own drawing function.
     * Called from the renderer to redraw this instance
     * with possible changes in values.
     *
     * @param gl     - The GL Context
     * @param filter - Which texture filter to be used
     */
    fun draw(gl: GL10, filter: Int) {
        //Bind the texture according to the set texture filter
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[filter])

        //Enable the vertex, texture and normal state
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY)

        //Set the face rotation
        gl.glFrontFace(GL10.GL_CCW)

        //Point to our buffers
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer)
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer)

        //Draw the vertices as triangles, based on the Index Buffer information
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.size, GL10.GL_UNSIGNED_BYTE, indexBuffer)

        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY)
    }

    /**
     * Load the textures
     *
     * @param gl      - The GL Context
     * @param context - The Activity context
     */
    fun loadGLTexture(gl: GL10, context: Context) {
        //Get the texture from the Android resource directory
        val inPut: InputStream = context.resources.openRawResource(R.raw.crate)
        val bitmap: Bitmap? = try {
            //BitmapFactory is an Android graphics utility for images
            BitmapFactory.decodeStream(inPut)
        } finally {
            //Always clear and close
            try {
                inPut.close()
            } catch (ignored: IOException) {
            }
        }

        //Generate there texture pointer
        gl.glGenTextures(3, textures, 0)

        //Create Nearest Filtered Texture and bind it to texture 0
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_NEAREST.toFloat()
        )
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_NEAREST.toFloat()
        )
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)

        //Create Linear Filtered Texture and bind it to texture 1
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1])
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)

        //Create mipmapped textures and bind it to texture 2
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[2])
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(
            GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR_MIPMAP_NEAREST.toFloat()
        )
        /*
         * This is a change to the original tutorial, as buildMipMap does not exist anymore
         * in the Android SDK.
         *
         * We check if the GL context is version 1.1 and generate MipMaps by flag.
         * Otherwise we call our own buildMipMap implementation
         */if (gl is GL11) {
            gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE.toFloat())
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)

            //
        } else {
            buildMipmap(bitmap)
        }

        //Clean up
        bitmap!!.recycle()
    }

    /**
     * Our own MipMap generation implementation.
     * Scale the original bitmap down, always by factor two,
     * and set it as new mipmap level.
     *
     *
     * Thanks to Mike Miller (with minor changes)!
     *
     * @param bitmap - The bitmap to mipmap
     */
    private fun buildMipmap(bitmap: Bitmap?) {
        //
        var bitmap = bitmap
        var level = 0
        //
        var height = bitmap!!.height
        var width = bitmap.width

        //
        while (height >= 1 || width >= 1) {
            //First of all, generate the texture from our bitmap and set it to the according level
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0)

            //
            if (height == 1 || width == 1) {
                break
            }

            //Increase the mipmap level
            level++

            //
            height /= 2
            width /= 2
            val bitmap2 = Bitmap.createScaledBitmap(bitmap!!, width, height, true)

            //Clean up
            bitmap.recycle()
            bitmap = bitmap2
        }
    }

    /**
     * The Cube constructor.
     *
     *
     * Initiate the buffers.
     */
    init {
        //
        //Vertices according to faces
        //v0
        //v1
        //v2
        //v3
        // ...
        val vertices = floatArrayOf( //Vertices according to faces
            -1.0f, -1.0f, 1.0f,  //v0
            1.0f, -1.0f, 1.0f,  //v1
            -1.0f, 1.0f, 1.0f,  //v2
            1.0f, 1.0f, 1.0f,  //v3
            1.0f, -1.0f, 1.0f,  // ...
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, -1.0f
        )
        var byteBuf = ByteBuffer.allocateDirect(vertices.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuf.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        //
        //Mapping coordinates for the vertices
        val texture = floatArrayOf( //Mapping coordinates for the vertices
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )
        byteBuf = ByteBuffer.allocateDirect(texture.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        textureBuffer = byteBuf.asFloatBuffer()
        textureBuffer.put(texture)
        textureBuffer.position(0)

        //
        // Normals
        val normals = floatArrayOf( // Normals
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
        )
        byteBuf = ByteBuffer.allocateDirect(normals.size * 4)
        byteBuf.order(ByteOrder.nativeOrder())
        normalBuffer = byteBuf.asFloatBuffer()
        normalBuffer.put(normals)
        normalBuffer.position(0)

        //
        indexBuffer = ByteBuffer.allocateDirect(indices.size)
        indexBuffer.put(indices)
        indexBuffer.position(0)
    }
}