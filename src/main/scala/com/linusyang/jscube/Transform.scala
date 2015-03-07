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
class Transform {
  var m: AffineMatrix = MatrixUtil.makeIdentityAffine()

  def reset() {
    m = MatrixUtil.makeIdentityAffine()
  }

  def rotateX(theta: Double) {
    m = MatrixUtil.multiplyAffine(MatrixUtil.makeRotateAffineX(theta), m)
  }

  def rotateY(theta: Double) {
    m = MatrixUtil.multiplyAffine(MatrixUtil.makeRotateAffineY(theta), m)
  }

  def rotateZ(theta: Double) {
    m = MatrixUtil.multiplyAffine(MatrixUtil.makeRotateAffineZ(theta), m)
  }

  def translate(dx: Double, dy: Double, dz: Double) {
    m = MatrixUtil.multiplyAffine(MatrixUtil.makeTranslateAffine(dx, dy, dz), m)
  }

  def scale(sx: Double, sy: Double, sz: Double) {
    m = MatrixUtil.multiplyAffine(MatrixUtil.makeTranslateAffine(sx, sy, sz), m)
  }

  def transformPoint(p: Vec) = MatrixUtil.transformPoint(m, p)

  def multTransform(t: Transform): Unit = {
    m = MatrixUtil.multiplyAffine(m, t.m)
  }

  def setDCM(u: Vec, v: Vec, w: Vec): Unit = {
    val m = this.m
    m.e0 = u.x; m.e4 = u.y; m.e8 = u.z
    m.e1 = v.x; m.e5 = v.y; m.e9 = v.z
    m.e2 = w.x; m.e6 = w.y; m.e10 = w.z
  }

  def dup() = {
    val tm = new Transform()
    tm.m = MatrixUtil.dupAffine(m)
    tm
  }
}
