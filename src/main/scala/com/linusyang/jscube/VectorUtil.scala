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
object VectorUtil {
  def crossProduct(a: Vec, b: Vec) = new Vec(
    a.y * b.z - a.z * b.y,
    a.z * b.x - a.x * b.z,
    a.x * b.y - a.y * b.x)

  def dotProduct3d(a: Vec, b: Vec): Double = a.x * b.x + a.y * b.y + a.z * b.z

  def subPoints3d(a: Vec, b: Vec) = new Vec(
    a.x - b.x,
    a.y - b.y,
    a.z - b.z
  )

  def subPoints2d(a: Vec, b: Vec) = new Vec(
    a.x - b.x,
    a.y - b.y,
    0
  )

  def addPoints2dIP(c: Vec, a: Vec, b: Vec) {
    c.x = a.x + b.x
    c.y = a.y + b.y
  }

  def subPoints2dIP(c: Vec, a: Vec, b: Vec) {
    c.x = a.x - b.x
    c.y = a.y - b.y
  }

  def mulPoint2d(a: Vec, s: Double) = new Vec(a.x * s, a.y * s, 0)
  def vecMag2d(a: Vec) = Math.sqrt(a.x * a.x + a.y * a.y)
  def unitVector2d(a: Vec) = mulPoint2d(a, 1.0 / vecMag2d(a))

  def mulPoint3d(a: Vec, s: Double) = new Vec(a.x * s, a.y * s, a.z * s)
  def vecMag3d(a: Vec) = Math.sqrt(a.x * a.x + a.y * a.y + a.z * a.z)
  def unitVector3d(a: Vec) = mulPoint3d(a, 1.0 / vecMag3d(a))

  def pushPoints2dIP(a: Vec, b: Vec) {
    val vec = unitVector2d(subPoints2d(b, a))
    addPoints2dIP(b, b, vec)
    subPoints2dIP(a, a, vec)
  }
}
