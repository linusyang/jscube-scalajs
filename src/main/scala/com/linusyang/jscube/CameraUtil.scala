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
import org.scalajs.dom.raw.{TouchEvent, Element, MouseEvent}
import org.scalajs.dom.window

/**
 * Created by linusyang on 3/7/15.
 */
object CameraUtil {
  type Listener = (CameraEventInfo) => Unit

  def autoCamera(grapher: Grapher, camera_state: CameraState): Unit = {
    val renderer = grapher.renderer
    val clamp = (a: Double, b: Double, c: Double) => Math.min(b, Math.max(a, c))
    var cur_pending: Int = -1
    val handleCameraMouse: Listener = (info: CameraEventInfo) => {
      if (info.is_clicking) {
        if (info.shift && info.ctrl) {
          camera_state.focal_length = clamp(0.05, 10,
            camera_state.focal_length + (info.delta_y * 0.005))
        } else if (info.shift) {
          camera_state.z += info.delta_y * 0.1
        } else if (info.ctrl) {
          camera_state.x -= info.delta_x * 0.02
          camera_state.y += info.delta_y * 0.02
        } else {
          camera_state.rotate_y -= info.delta_x * 0.01
          camera_state.rotate_x -= info.delta_y * 0.01
        }
      }
      if (cur_pending != -1) {
        window.clearTimeout(cur_pending)
      }
      val timeout_func = () => {
        cur_pending = -1
        CameraUtil.setCamera(grapher, camera_state)
      }
      cur_pending = window.setTimeout(timeout_func, 0)
    }
    registerMouseListener(renderer.canvas, handleCameraMouse)
    registerTouchListener(renderer.canvas, handleCameraMouse)
    setCamera(grapher, camera_state, draw_now = false)
  }

  def setCamera(grapher: Grapher, state: CameraState, draw_now: Boolean = true): Unit = {
    val renderer = grapher.renderer
    renderer.camera.focal_length = state.focal_length
    val ct = renderer.camera.transform
    ct.reset()
    ct.rotateZ(state.rotate_z)
    ct.rotateY(state.rotate_y)
    ct.rotateX(state.rotate_x)
    ct.translate(state.x, state.y, state.z)
    if (draw_now) {
      grapher.draw()
    }
  }

  def registerMouseListener(canvas: Canvas, listener: Listener): Unit = {
    var first_event = true
    var is_clicking = false
    var last_x = 0.0
    var last_y = 0.0

    val relXY = (e: MouseEvent) => {
      var target = e.srcElement
      if (target == null) {
        target = e.target.asInstanceOf[Element]
      }
      val rect = target.getBoundingClientRect()
      val offsetX = e.clientX - rect.left
      val offsetY = e.clientY - rect.top
      new Vec(offsetX, offsetY)
    }

    val mouseDownEvent = (e: MouseEvent) => {
      val rel = relXY(e)
      is_clicking = true
      last_x = rel.x
      last_y = rel.y
      e.preventDefault()
      false
    }
    val mouseUpEvent = (e: MouseEvent) => {
      is_clicking = false
      e.preventDefault()
      false
    }
    val mouseOutEvent = mouseUpEvent
    val mouseMoveEvent = (e: MouseEvent) => {
      if (is_clicking) {
        val rel = relXY(e)
        val delta_x = last_x - rel.x
        val delta_y = last_y - rel.y
        last_x = rel.x
        last_y = rel.y
        if (first_event) {
          first_event = false
        } else {
          listener(new CameraEventInfo(
            is_clicking,
            delta_x, delta_y,
            e.shiftKey, e.ctrlKey || e.metaKey)
          )
        }
      }
      e.preventDefault()
      false
    }

    canvas.addEventListener("mousedown", mouseDownEvent, false)
    canvas.addEventListener("mouseup", mouseUpEvent, false)
    canvas.addEventListener("mouseout", mouseOutEvent, false)
    canvas.addEventListener("mousemove", mouseMoveEvent, false)
  }

  def registerTouchListener(canvas: Canvas, listener: Listener): Unit = {
    val scroll_threshold = 5
    var first_event = true
    var is_clicking = false
    var two_finger = false
    var last_x0 = 0.0
    var last_y0 = 0.0
    var last_x1 = 0.0
    var last_y1 = 0.0

    val getDist = (x0: Double, x1: Double, y0: Double, y1: Double) => {
      Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1))
    }

    val touchStartEvent = (e: TouchEvent) => {
      is_clicking = true
      last_x0 = e.touches(0).clientX
      last_y0 = e.touches(0).clientY
      if (e.touches.length == 2) {
        two_finger = true
        last_x1 = e.touches(1).clientX
        last_y1 = e.touches(1).clientY
      }
      e.preventDefault()
      false
    }
    val touchEndEvent = (e: TouchEvent) => {
      is_clicking = false
      two_finger = false
      e.preventDefault()
      false
    }
    val touchMoveEvent = (e: TouchEvent) => {
      var is_scroll = false
      var is_pinch = false
      var delta_x = 0.0
      var delta_y = 0.0
      if (two_finger && e.touches.length == 2) {
        val last_dist = getDist(last_x0, last_x1, last_y0, last_y1)
        val now_dist = getDist(e.touches(0).clientX,
          e.touches(1).clientX,
          e.touches(0).clientY,
          e.touches(1).clientY)
        val delta_dist = now_dist - last_dist
        is_scroll = delta_dist > -scroll_threshold && delta_dist < scroll_threshold
        is_pinch = !is_scroll
        delta_x = last_x0 - e.touches(0).clientX
        if (is_scroll) {
          delta_y = last_y0 - e.touches(0).clientY
        } else {
          delta_y = delta_dist
        }
        last_x1 = e.touches(1).clientX
        last_y1 = e.touches(1).clientY
      } else {
        delta_x = last_x0 - e.touches(0).clientX
        delta_y = last_y0 - e.touches(0).clientY
      }
      last_x0 = e.touches(0).clientX
      last_y0 = e.touches(0).clientY
      if (first_event) {
        first_event = false
      } else {
        listener(new CameraEventInfo(
          is_clicking,
          delta_x, delta_y,
          is_pinch, is_scroll)
        )
      }
      e.preventDefault()
      false
    }

    canvas.addEventListener("touchstart", touchStartEvent, false)
    canvas.addEventListener("touchend", touchEndEvent, false)
    canvas.addEventListener("touchmove", touchMoveEvent, false)
  }
}
