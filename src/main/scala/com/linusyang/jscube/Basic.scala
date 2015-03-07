/*
 * JSCube
 *
 * Simple HTML5 3D engine powered by Scala.js
 * https://github.com/linusyang/jscube-scala.js
 *
 * Copyright (c) 2013-2015 Linus Yang
 */

package com.linusyang.jscube

/**
 * Created by linusyang on 3/7/15.
 */

class Vec(var x: Double, var y: Double, var z: Double = 0)

class AffineMatrix(var e0: Double, var e1: Double, var e2: Double, var e3: Double,
                   var e4: Double, var e5: Double, var e6: Double, var e7: Double,
                   var e8: Double, var e9: Double, var e10: Double, var e11: Double)

class RGBA(var r: Double, var g: Double, var b: Double, var a: Double)

class QuadFace(var v0: Int, var v1: Int, var v2: Int, var v3: Int = -1) {
  var centroid: Vec = _
  var normal1: Vec = _
  var normal2: Vec = _
  var i0: Vec = _
  var i1: Vec = _
  var i2: Vec = _
  var i3: Vec = _

  def isTriangle() = v3 == -1
}

class Shape {
  var vertices: List[Vec] = _
  var quads: List[QuadFace] = _
}

class Camera {
  var transform = new Transform()
  var focal_length: Double = 1.0
}

class QuadBuffer(var qf: QuadFace, var intensity: Double,
                 var draw_overdraw: Boolean, var fill_rgba: RGBA)

class GraphShape(var shape: Shape, var color: RGBA, var trans: Transform)

class CameraState(var x: Double, var y: Double, var z: Double,
                  var rotate_x: Double, var rotate_y: Double, var rotate_z: Double,
                  var focal_length: Double) {
  def dup() = new CameraState(x, y, z, rotate_x, rotate_y, rotate_z, focal_length)
  def setCameraState(s: CameraState): Unit = {
    this.x = s.x
    this.y = s.y
    this.z = s.z
    this.rotate_x = s.rotate_x
    this.rotate_y = s.rotate_y
    this.rotate_z = s.rotate_z
    this.focal_length = s.focal_length
  }
}

class CameraEventInfo(var is_clicking: Boolean,
                      var delta_x: Double, var delta_y: Double,
                      var shift: Boolean, var ctrl: Boolean)
