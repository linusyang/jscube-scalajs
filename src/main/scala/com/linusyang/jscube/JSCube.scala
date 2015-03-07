/*
 * JSCube
 *
 * Simple HTML5 3D engine powered by Scala.js
 * https://github.com/linusyang/jscube-scala.js
 *
 * Copyright (c) 2013-2015 Linus Yang
 */

package com.linusyang.jscube

import org.scalajs.dom.raw.MouseEvent

import scala.scalajs.js
import scala.scalajs.js.JSApp
import org.scalajs.dom.{KeyboardEvent, window, document, navigator}
import org.scalajs.jquery.{JQueryStatic, JQuery}
import org.scalajs.dom.html.Canvas

import scala.scalajs.js.annotation.JSExport

/**
 * Created by linusyang on 3/7/15.
 */
trait JQueryModal extends JQuery {
  def modal(action: String): JQuery = js.native
}

object jQuery extends JQueryStatic {
  override def apply(selector: String): JQueryModal = js.native
}

@JSExport
object JSCube extends JSApp {
  @JSExport
  def init(): Unit = {
    // Detect mobile device
    var always_path = true
    var is_mobile_device = false
    if (navigator.userAgent matches("(?i).*Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini.*")) {
      is_mobile_device = true
    }

    // Grapher initialize
    val canvas: Canvas = jQuery("#canvas").get(0).asInstanceOf[Canvas]
    val grapher = new Grapher(canvas)
    val mq = window.matchMedia("only screen and (min--moz-device-pixel-ratio: 1.3), only screen and (-o-min-device-pixel-ratio: 2.6/2), only screen and (-webkit-min-device-pixel-ratio: 1.3), only screen  and (min-device-pixel-ratio: 1.3), only screen and (min-resolution: 1.3dppx)")
    if (mq != null && mq.matches) {
      grapher.scale = 2.0
    }
    grapher.genCubes(false)
    var current_color = "color-random"

    // Set camera
    val state = new CameraState(0, 0, -30, 0.40, -1.06, 0, 2.5)
    val orig_state = state.dup()
    CameraUtil.autoCamera(grapher, state)

    // Callback funtions
    val resizeCanvas: () => Unit = () => {
      val w = jQuery("#canvas-wrapper").width()
      val h = jQuery("#canvas-wrapper").height()
      val sw = Math.floor(w * grapher.scale).toInt
      val sh = Math.floor(h * grapher.scale).toInt
      jQuery("#canvas").attr("width", s"${sw}px")
      jQuery("#canvas").attr("height", s"${sh}px")
      if (grapher.scale > 1) {
        jQuery("#canvas").css("width", s"${w}px")
        jQuery("#canvas").css("height", s"${h}px")
      }
      grapher.setSize(sw, sh)
    }

    val resizeDraw: () => Unit = () => {
      resizeCanvas()
      grapher.draw()
    }

    val resetCamera: () => Unit = () => {
      state.setCameraState(orig_state)
      CameraUtil.setCamera(grapher, state)
    }

    val changeBackground: () => Unit = () => {
      jQuery("#canvas-wrapper").css("background-color", if (grapher.bg_white) "black" else "white")
      grapher.toggleBackground()
    }

    val togglePath: () => Unit = () => {
      if (grapher.isUsePath()) {
        jQuery("#action-anti").removeClass("disabled")
      } else {
        jQuery("#action-anti").addClass("disabled")
      }
      resizeCanvas()
      grapher.togglePath()
    }

    val changePath: () => Unit = () => {
      if (is_mobile_device && grapher.isUsePath()) {
        jQuery("#path-modal").modal("show")
      } else {
        togglePath()
      }
    }

    // Window event
    var res = -1
    jQuery(window).resize(() => {
      if (res != -1) {
        window.clearTimeout(res)
      }
      res = window.setTimeout(resizeDraw, 500)
    })

    jQuery(document).bind("keydown", (e: KeyboardEvent) => {
      if (e != null) {
        e.keyCode match {
          case 66 => changeBackground() // 'b'
          case 65 => grapher.toggleAntiAlias() // 'a'
          case 80 => changePath() // 'p'
          case 82 => resetCamera() // 'r'
          case default => {}
        }
      }
    })

    // Menu actions
    jQuery("#action-color").click((e: MouseEvent) => {
      if (jQuery("#action-color").hasClass("disabled")) {
        e.preventDefault()
        false
      } else {
        jQuery("#color-modal").modal("show")
        true
      }
    })

    jQuery("#color-cancel").click((e: MouseEvent) => {
      e.preventDefault()
      jQuery("#" + current_color).prop("checked", true)
      jQuery("#color-modal").modal("hide")
    })

    jQuery("#color-ok").click((e: MouseEvent) => {
      e.preventDefault()
      current_color = jQuery("input[name=colorRadios]:checked") attr "id"
      grapher.drawWithColor(current_color)
      jQuery("#color-modal") modal "hide"
    })

    jQuery("#action-reset").click(() => {
      resetCamera()
    })

    jQuery("#action-background").click(() => {
      changeBackground()
    })

    jQuery("#action-anti").click((e: MouseEvent) => {
      grapher.toggleAntiAlias()
      if (grapher.isUsePath()) {
        e.preventDefault()
        false
      } else {
        true
      }
    })

    jQuery("#action-path").click(() => {
      changePath()
    })

    jQuery("#action-help").click(() => {
      jQuery("#help-modal") modal "show"
    })

    jQuery("#help-ok").click((e: MouseEvent) => {
      e.preventDefault()
      jQuery("#help-modal") modal "hide"
    })

    jQuery("#path-cancel").click((e: MouseEvent) => {
      e.preventDefault()
      jQuery("#path-modal") modal "hide"
    })

    jQuery("#path-ok").click((e: MouseEvent) => {
      togglePath()
      e.preventDefault()
      jQuery("#path-modal") modal "hide"
    })

    // Navbar actions
    jQuery("#cube-nav li").click((e: MouseEvent) => {
      jQuery("#cube-nav li.active") removeClass "active"
      if (!(jQuery(e.currentTarget) hasClass "active")) {
        jQuery(e.currentTarget) addClass "active" // use e.currentTarget instead of "this"
      }
      if (jQuery("#action-color") hasClass "disabled") {
        jQuery("#action-color") removeClass "disabled"
      }
      e.preventDefault()
    })

    jQuery("#bar-cube").click(() => {
      grapher.genCubes(false)
      grapher.drawWithColor(current_color)
    })

    jQuery("#bar-rcube").click(() => {
      if (!(jQuery("#action-color") hasClass "disabled")) {
        jQuery("#action-color") addClass "disabled"
      }
      grapher.genCubes(true)
      grapher.drawWithColor(current_color)
    })

    jQuery("#bar-twenty").click(() => {
      grapher.genTwenty()
      grapher.drawWithColor(current_color)
    })

    if (always_path || is_mobile_device) {
      grapher.disableAntiAlias()
      togglePath()
    } else {
      resizeDraw()
    }
  }

  def main(): Unit = {
    println("Please run JSCube in browser.")
  }
}
