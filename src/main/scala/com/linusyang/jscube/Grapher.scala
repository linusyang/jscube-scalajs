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
class Grapher(var canvas: Canvas) extends RendererCallback{
  val renderer = new Renderer(canvas)
  var gshapes: List[GraphShape] = _
  val dtrans = new Transform()
  var bg_white = true
  var scale: Double = 1.0

  val black_color = new RGBA(0, 0, 0, 1)
  val white_color = new RGBA(1, 1, 1, 1)
  val red_color = new RGBA(0.86, 0.08, 0.24, 1)
  val orange_color = new RGBA(1.00, 0.55, 0.00, 1)
  val green_color = new RGBA(0.20, 0.80, 0.20, 1)
  val cyan_color = new RGBA(0.25, 0.41, 0.88, 1)
  val purple_color = new RGBA(0.58, 0.00, 0.83, 1)
  val pool_num = 20
  var color_str = "color-random"
  val quad_randcolor: QuadCallBack = (renderer: Renderer, quad_face: QuadFace,
                                      quad_index: Int, shape: Shape) => {
    renderer.fill_rgba = renderer.color_pool(quad_index % renderer.color_pool_num)
    false
  }
  val quad_fixcolor: QuadCallBack = (renderer: Renderer, quad_face: QuadFace,
                                     quad_index: Int, shape: Shape) => {
    renderer.fill_rgba = renderer.quad_color
    false
  }
  var quad_callback: QuadCallBack = _

  genRandColor(pool_num)

  def draw(): Unit = {
    if (gshapes != null) {
      renderer.resetCanvas()
      renderer.quad_callback = quad_callback
      for (s <- gshapes) {
        renderer.fill_rgba = if (s.color == null) white_color else s.color
        renderer.transform = if (s.trans == null) dtrans else s.trans
        renderer.bufferShape(s.shape)
      }
      drawBackground()
      renderer.drawBuffer()
    }
  }

  def setSize(w: Int, h: Int): Unit = {
    renderer.setSize(w, h)
  }

  def drawBackground(): Unit = {
    var bg_rgba = black_color
    if (bg_white) {
      bg_rgba = white_color
    }
    if (renderer.ctx.use_path) {
      renderer.drawBackground(bg_rgba)
    }
  }

  def toggleBackground(): Unit = {
    bg_white = !bg_white
    draw()
  }

  def toggleAntiAlias(): Unit = {
    if (!isUsePath()) {
      renderer.ctx.anti_alias = !renderer.ctx.anti_alias
      draw()
    }
  }

  def disableAntiAlias(): Unit = {
    renderer.ctx.anti_alias = false
  }

  def isUsePath() = renderer.ctx.use_path

  def togglePath(): Unit = {
    renderer.ctx.use_path = !renderer.ctx.use_path
    draw()
  }

  def genRandColor(num: Int): Unit = {
    renderer.color_pool_num = num
    renderer.color_pool = ArrayBuffer()
    var i = 0
    while (i <= renderer.color_pool_num) {
      renderer.color_pool.append(new RGBA(Math.random(), Math.random(), Math.random(), 1))
      i += 1
    }
  }

  def drawWithColor(color_str: String): Unit = {
    if (this.color_str != "color-custom") {
      setColor(color_str)
    }
    draw()
  }

  def setColor(color_str: String): Unit = {
    this.color_str = color_str
    var quad_color = white_color
    val isFixedColor = color_str match {
      case "color-custom" => quad_callback = null; false
      case "color-random" => genRandColor(pool_num); quad_callback = quad_randcolor; false
      case "color-white" => quad_color = white_color; true
      case "color-red" => quad_color = red_color; true
      case "color-orange" => quad_color = orange_color; true
      case "color-green" => quad_color = green_color; true
      case "color-cyan" => quad_color = cyan_color; true
      case "color-purple" => quad_color = purple_color; true
    }
    if (isFixedColor) {
      renderer.quad_color = quad_color
      quad_callback = quad_fixcolor
    }
  }

  def genCubes(colorcubes: Boolean): Unit = {
    val cubes = new ArrayBuffer[GraphShape]()
    if (colorcubes) {
      setColor("color-custom")
      val edge: Double = 4.0 / 3
      for (i <- 0 to 2) {
        for (j <- 0 to 2) {
          for (k <- 0 to 2) {
            if (i == 0 || j == 0 || k == 0 || i == 2 || j == 2 || k == 2) {
              val cube = ShapeUtil.makeCube(edge)
              val transform = new Transform()
              transform.translate(2 * edge * (i - 1), 2 * edge * (j - 1), 2 * edge * (k - 1))
              cubes.append(new GraphShape(cube, new RGBA(i / 3.0, j / 3.0, k / 3.0, 0.3), transform))
            }
          }
        }
      }
    } else {
      setColor("color-random")
      cubes.append(new GraphShape(ShapeUtil.makeCube(4), null, null))
    }
    gshapes = cubes.toList
  }

  def genTwenty(): Unit = {
    setColor("color-random")
    gshapes = List(new GraphShape(ShapeUtil.makeTwenty(3), null, null))
  }
}
