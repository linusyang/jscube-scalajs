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
import org.scalajs.dom.ImageData
import scala.collection.mutable

/**
 * Created by linusyang on 3/7/15.
 */

class Context(canvas: Canvas) {
  val ctx_ = canvas.getContext("2d")
  var use_path = false
  var anti_alias = true
  var bg_rgba = new RGBA(1, 1, 1, 1)
  var width: Int = 0
  var height: Int = 0
  var buffer: ImageData = _

  setSize(canvas.width, canvas.height)
  emptyBuffer()

  def emptyBuffer(): Unit = {
    if (!use_path) {
      buffer = ctx_.createImageData(width, height).asInstanceOf[ImageData]
    }
  }

  def drawBuffer(): Unit = {
    if (!use_path) {
      ctx_.putImageData(buffer, 0, 0)
    }
  }

  def setSize(w: Int, h: Int): Unit = {
    width = w
    height = h
  }

  def resetCanvas(): Unit = {
    if (!use_path) {
      emptyBuffer()
      drawBuffer()
    } else {
      ctx_.clearRect(0, 0, width, height)
    }
  }

  def fillRect(x: Double, y: Double, w: Double, h: Double): Unit = {
    if (use_path) {
      ctx_.fillRect(x, y, w, h)
    } else {
      val data = buffer.data
      val orig_r = bg_rgba.r * 255.0
      val orig_g = bg_rgba.g * 255.0
      val orig_b = bg_rgba.b * 255.0
      val orig_a = bg_rgba.a * 255.0
      var pos = ((y * width + x) * 4).toInt
      var j = y
      while (j <= y + w) {
        var i = x
        while (i <= x + h) {
          data(pos) = orig_r.toInt
          data(pos + 1) = orig_g.toInt
          data(pos + 2) = orig_b.toInt
          data(pos + 3) = orig_a.toInt
          pos += 4
          i += 1
        }
        j += 1
      }
      drawBuffer()
    }
  }

  def setFillColor(r: Double, g: Double, b: Double, a: Double): Unit = {
    val rgba = List(
      Math.floor(r * 255.0),
      Math.floor(g * 255.0),
      Math.floor(b * 255.0),
      a
    )
    ctx_.fillStyle = s"rgba(${rgba.mkString(",")})"
  }

  //  Half-space triangle fill algorithm:
  //  http://devmaster.net/posts/6145/advanced-rasterization
  def fillTriangle(v1: Vec, v2: Vec, v3: Vec, rgba: RGBA, border: mutable.Map[Int, Double]): Unit = {
    // 28.4 fixed-pocoordinates
    val Y1 = Math.round(16.0 * v1.y)
    val Y2 = Math.round(16.0 * v2.y)
    val Y3 = Math.round(16.0 * v3.y)

    val X1 = Math.round(16.0 * v1.x)
    val X2 = Math.round(16.0 * v2.x)
    val X3 = Math.round(16.0 * v3.x)

    // Deltas
    val DX12 = X1 - X2
    val DX23 = X2 - X3
    val DX31 = X3 - X1

    val DY12 = Y1 - Y2
    val DY23 = Y2 - Y3
    val DY31 = Y3 - Y1

    // Fixed-podeltas
    val FDX12 = DX12 << 4
    val FDX23 = DX23 << 4
    val FDX31 = DX31 << 4

    val FDY12 = DY12 << 4
    val FDY23 = DY23 << 4
    val FDY31 = DY31 << 4

    // Bounding rectangle
    val minx = (Math.min(Math.min(X1, X2), X3) + 0xF) >> 4
    val maxx = (Math.max(Math.max(X1, X2), X3) + 0xF) >> 4
    val miny = (Math.min(Math.min(Y1, Y2), Y3) + 0xF) >> 4
    val maxy = (Math.max(Math.max(Y1, Y2), Y3) + 0xF) >> 4

    // Half-edge constants
    var C1 = DY12 * X1 - DX12 * Y1
    var C2 = DY23 * X2 - DX23 * Y2
    var C3 = DY31 * X3 - DX31 * Y3

    // Correct for fill convention
    if (DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1 += 1
    if (DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2 += 1
    if (DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3 += 1

    var CY1 = C1 + DX12 * (miny << 4) - DY12 * (minx << 4)
    var CY2 = C2 + DX23 * (miny << 4) - DY23 * (minx << 4)
    var CY3 = C3 + DX31 * (miny << 4) - DY31 * (minx << 4)

    val data = buffer.data
    val R1 = rgba.r * 255.0
    val G1 = rgba.g * 255.0
    val B1 = rgba.b * 255.0
    val a1 = rgba.a
    var y = miny
    var pos_start = (miny * width + minx) * 4
    var pos_delta = width * 4

    while (y < maxy) {
      var CX1 = CY1
      var CX2 = CY2
      var CX3 = CY3
      var x = minx
      var pos = pos_start.toInt
      while (x < maxx) {
        var a2 = 0.0
        if (CX1 > 0 && CX2 > 0 && CX3 > 0)
          a2 = a1
        else if (anti_alias && border.contains(pos))
          a2 = border.getOrElse(pos, 0.0) * a1
        // Alpha compositing
        //   http://en.wikipedia.org/wiki/Alpha_compositing
        if (a2 > 0) {
          var R0: Double = data(pos)
          var G0: Double = data(pos + 1)
          var B0: Double = data(pos + 2)
          val a0: Double = data(pos + 3) / 255.0
          val fa2 = 1 - a2
          val na = a2 + a0 * fa2
          R0 = (R1 * a2 + fa2 * R0 * a0) / na
          G0 = (G1 * a2 + fa2 * G0 * a0) / na
          B0 = (B1 * a2 + fa2 * B0 * a0) / na
          data(pos) = R0.toInt
          data(pos + 1) = G0.toInt
          data(pos + 2) = B0.toInt
          data(pos + 3) = (na * 255.0).toInt
        }
        CX1 -= FDY12
        CX2 -= FDY23
        CX3 -= FDY31
        x += 1
        pos += 4
      }
      CY1 += FDX12
      CY2 += FDX23
      CY3 += FDX31
      y += 1
      pos_start += pos_delta
    }
  }

  // Xiaolin Wu's line algorithm
  //   http://en.wikipedia.org/wiki/Xiaolin_Wu%27s_line_algorithm
  def drawWuLine(v0: Vec, v1: Vec, border: mutable.Map[Int, Double]): Unit = {
    var x0 = v0.x
    var x1 = v1.x
    var y0 = v0.y
    var y1 = v1.y

    val steep = Math.abs(y1 - y0) > Math.abs(x1 - x0)
    if (steep) {
      val t1 = x0
      x0 = y0
      y0 = t1
      val t2 = x1
      x1 = y1
      y1 = t2
    }
    if (x0 > x1) {
      val t1 = x0
      x0 = x1
      x1 = t1
      val t2 = y0
      y0 = y1
      y1 = t2
    }
    val dx = x1 - x0
    val dy = y1 - y0
    val gradient = dy / dx

    val width_4 = width * 4
    val ipart = (x: Double) => Math.floor(x)
    val round = (x: Double) => Math.round(x)
    val rfpart = (x: Double) => 1 - (x - Math.floor(x))
    val dual_plot = (x1: Double, y1: Double, c1: Double, s: Double) => {
      var x = x1
      var y = y1
      var c = c1
      if (steep) {
        val t = x
        x = y
        y = t
      }
      val pos = ((y * width + x) * 4).toInt
      c -= Math.floor(c)
      val t = c * s
      border(pos) = s - t
      if (steep) {
        border(pos + 4) = t
      } else {
        border(pos + width_4) = t
      }
    }

    // handle first endpoint
    var xend = round(x0)
    var yend = y0 + gradient * (xend - x0)
    var xgap = rfpart(x0) + 0.5
    val xpxl1 = xend
    val ypxl1 = ipart(yend)
    dual_plot(xpxl1, ypxl1, yend, xgap)

    // initialize start y
    var intery = yend + gradient

    // handle second endpoint
    xend = round(x1)
    yend = y1 + gradient * (xend - x1)
    xgap = rfpart(x1 + 0.5)
    val xpxl2 = xend
    val ypxl2 = ipart(yend)
    dual_plot(xpxl2, ypxl2, yend, xgap)

    var x = xpxl1 + 1
    while (x < xpxl2) {
      dual_plot(x, ipart(intery), intery, 1)
      x += 1
      intery += gradient
    }
  }

  def fillQuadFace(qf: QuadFace, rgba: RGBA): Unit = {
    if (!use_path) {
      val border = new mutable.HashMap[Int, Double]()
      if (qf.isTriangle()) {
        if (anti_alias) {
          drawWuLine(qf.i0, qf.i1, border)
          drawWuLine(qf.i1, qf.i2, border)
          drawWuLine(qf.i2, qf.i0, border)
        }
        fillTriangle(qf.i0, qf.i1, qf.i2, rgba, border)
      } else {
        if (anti_alias) {
          drawWuLine(qf.i0, qf.i1, border)
          drawWuLine(qf.i1, qf.i2, border)
          drawWuLine(qf.i2, qf.i3, border)
          drawWuLine(qf.i3, qf.i0, border)
        }
        fillTriangle(qf.i0, qf.i1, qf.i2, rgba, border)
        fillTriangle(qf.i2, qf.i3, qf.i0, rgba, border)
      }
    } else {
      ctx_.beginPath()
      ctx_.moveTo(qf.i0.x, qf.i0.y)
      ctx_.lineTo(qf.i1.x, qf.i1.y)
      ctx_.lineTo(qf.i2.x, qf.i2.y)
      if (!qf.isTriangle()) {
        ctx_.lineTo(qf.i3.x, qf.i3.y)
      }
      setFillColor(rgba.r, rgba.g, rgba.b, rgba.a)
      ctx_.fill()
    }
  }
}
