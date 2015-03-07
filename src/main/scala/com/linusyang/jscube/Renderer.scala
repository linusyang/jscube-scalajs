/*
 * JSCube
 *
 * Simple HTML5 3D engine powered by Scala.js
 * https://github.com/linusyang/jscube-scala.js
 *
 * Copyright (c) 2013-2015 Linus Yang
 */

package com.linusyang.jscube

import org.scalajs.dom.html.Canvas
import scala.collection.mutable.ArrayBuffer

/**
 * Created by linusyang on 3/7/15.
 */

trait RendererCallback {
  type QuadCallBack = (Renderer, QuadFace, Int, Shape) => Boolean
}

class Renderer(var canvas: Canvas) extends RendererCallback{
  var perform_z_sorting = true
  var draw_overdraw = true
  var draw_backfaces = false
  var fill_rgba = new RGBA(1, 0, 0, 1)
  var ctx = new Context(canvas)
  var camera = new Camera()
  var transform = new Transform()
  var color_pool: ArrayBuffer[RGBA] = _
  var color_pool_num = 0
  var quad_color: RGBA = _

  // A callback allows you to change the render state per-quad, and
  // also to skip a quad by returning true from the callback:
  //   renderer.quad_callback = (renderer, quad_face, quad_index, shape) ->
  //     renderer.fill_rgba.r = quad_index * 0.1
  //     return false  # Don't skip this quad
  var quad_callback: QuadCallBack = _
  var buffered_quads_ = new ArrayBuffer[QuadBuffer]()

  var width_ = 0
  var height_ = 0
  var scale_ : Double = 0
  var xoff_ : Double = 0

  setSize(canvas.width, canvas.height)
  emptyBuffer()

  def setSize(w: Int, h: Int): Unit = {
    width_ = w
    height_ = h
    scale_ = height_ / 2.0
    xoff_ = width_ / 2.0
    canvas.width = w
    canvas.height = h
    ctx.setSize(w, h)
  }

  def emptyBuffer(): Unit = {
    buffered_quads_.clear()
    ctx.emptyBuffer()
  }

  def resetCanvas(): Unit = {
    buffered_quads_.clear()
    ctx.resetCanvas()
  }

  // Pinhole camera model
  def projectPointToCanvas(p: Vec) = {
    val v = camera.focal_length / -p.z
    new Vec(p.x * v * scale_ + xoff_,
            p.y * v * -scale_ + scale_)
  }

  def projectQuadFaceToCanvasIP(qf: QuadFace): Unit = {
    qf.i0 = projectPointToCanvas(qf.i0)
    qf.i1 = projectPointToCanvas(qf.i1)
    qf.i2 = projectPointToCanvas(qf.i2)
    if (!qf.isTriangle()) {
      qf.i3 = projectPointToCanvas(qf.i3)
    }
  }

  def bufferShape(shape: Shape): Unit = {
    val t = MatrixUtil.multiplyAffine(camera.transform.m, transform.m)
    val tn = MatrixUtil.transAdjoint(t)
    val world_vertices = MatrixUtil.transformPoints(t, shape.vertices)
    val processFace = (qf: QuadFace, j: Int) => {
      if (quad_callback == null || !quad_callback(this, qf, j, shape)) {
        val centroid = MatrixUtil.transformPoint(t, qf.centroid)
        if (centroid.z < -1) {
          val n1 = VectorUtil.unitVector3d(MatrixUtil.transformPoint(tn, qf.normal1))
          val n2 = MatrixUtil.transformPoint(tn, qf.normal2)
          if (!(!draw_backfaces &&
            VectorUtil.dotProduct3d(centroid, n1) > 0 &&
            VectorUtil.dotProduct3d(centroid, n2) > 0)) {

            var intensity = VectorUtil.dotProduct3d(new Vec(0, 0, 1), n1)
            if (intensity < 0) {
              intensity = 0
            }

            val world_qf = new QuadFace(qf.v0, qf.v1, qf.v2, qf.v3)
            world_qf.i0 = world_vertices(qf.v0)
            world_qf.i1 = world_vertices(qf.v1)
            world_qf.i2 = world_vertices(qf.v2)
            if (!qf.isTriangle()) {
              world_qf.i3 = world_vertices(qf.v3)
            }
            world_qf.centroid = centroid
            world_qf.normal1 = n1
            world_qf.normal2 = n2

            buffered_quads_.append(new QuadBuffer(world_qf, intensity, draw_overdraw, fill_rgba))
          }
        }
      }
    }
    var j = 0
    for (qf <- shape.quads) {
      processFace(qf, j)
      j += 1
    }
  }

  def drawBackground(rgba: RGBA): Unit = {
    ctx.setFillColor(rgba.r, rgba.g, rgba.b, rgba.a)
    ctx.bg_rgba = rgba
    ctx.fillRect(0, 0, width_, height_)
  }

  def drawBuffer(): Unit = {
    val all_quads = buffered_quads_
    if (perform_z_sorting) {
      all_quads.sortWith { (x, y) => x.qf.centroid.z < y.qf.centroid.z }
    }
    for (obj <- all_quads) {
      val qf = obj.qf
      projectQuadFaceToCanvasIP(qf)
      val is_triangle = qf.isTriangle()
      if (obj.draw_overdraw) {
        VectorUtil.pushPoints2dIP(qf.i0, qf.i1)
        VectorUtil.pushPoints2dIP(qf.i1, qf.i2)
        if (is_triangle) {
          VectorUtil.pushPoints2dIP(qf.i2, qf.i0)
        } else {
          VectorUtil.pushPoints2dIP(qf.i2, qf.i3)
          VectorUtil.pushPoints2dIP(qf.i3, qf.i0)
        }
      }
      val iy = obj.intensity
      val frgba = obj.fill_rgba
      val rgba = new RGBA(frgba.r * iy, frgba.g * iy, frgba.b * iy, frgba.a)
      ctx.fillQuadFace(qf, rgba)
    }
    ctx.drawBuffer()
  }
}
