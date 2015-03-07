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

object MatrixUtil {
  def dupAffine(m: AffineMatrix) = new AffineMatrix(
    m.e0, m.e1, m.e2, m.e3,
    m.e4, m.e5, m.e6, m.e7,
    m.e8, m.e9, m.e10, m.e11)

  def multiplyAffine(a: AffineMatrix, b: AffineMatrix) = new AffineMatrix(
    a.e0 * b.e0 + a.e1 * b.e4 + a.e2 * b.e8,
    a.e0 * b.e1 + a.e1 * b.e5 + a.e2 * b.e9,
    a.e0 * b.e2 + a.e1 * b.e6 + a.e2 * b.e10,
    a.e0 * b.e3 + a.e1 * b.e7 + a.e2 * b.e11 + a.e3,
    a.e4 * b.e0 + a.e5 * b.e4 + a.e6 * b.e8,
    a.e4 * b.e1 + a.e5 * b.e5 + a.e6 * b.e9,
    a.e4 * b.e2 + a.e5 * b.e6 + a.e6 * b.e10,
    a.e4 * b.e3 + a.e5 * b.e7 + a.e6 * b.e11 + a.e7,
    a.e8 * b.e0 + a.e9 * b.e4 + a.e10 * b.e8,
    a.e8 * b.e1 + a.e9 * b.e5 + a.e10 * b.e9,
    a.e8 * b.e2 + a.e9 * b.e6 + a.e10 * b.e10,
    a.e8 * b.e3 + a.e9 * b.e7 + a.e10 * b.e11 + a.e11
  )

  def makeIdentityAffine() = new AffineMatrix(
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0
  )

  def makeTranslateAffine(dx: Double, dy: Double, dz: Double) = new AffineMatrix(
    1, 0, 0, dx,
    0, 1, 0, dy,
    0, 0, 1, dz
  )

  def makeScaleAffine(sx: Double, sy: Double, sz: Double) = new AffineMatrix(
    sx, 0, 0, 0,
    0, sy, 0, 0,
    0, 0, sz, 0
  )

  def makeRotateAffineX(theta: Double) = {
    val s = Math.sin(theta)
    val c = Math.cos(theta)
    new AffineMatrix(
      1, 0, 0, 0,
      0, c, -s, 0,
      0, s, c, 0
    )
  }

  def makeRotateAffineY(theta: Double) = {
    val s = Math.sin(theta)
    val c = Math.cos(theta)
    new AffineMatrix(
      c, 0, s, 0,
      0, 1, 0, 0,
      -s, 0, c, 0
    )
  }

  def makeRotateAffineZ(theta: Double) = {
    val s = Math.sin(theta)
    val c = Math.cos(theta)
    new AffineMatrix(
      c, -s, 0, 0,
      s, c, 0, 0,
      0, 0, 1, 0
    )
  }

  def transAdjoint(a: AffineMatrix) = new AffineMatrix(
    a.e10 * a.e5 - a.e6 * a.e9,
    a.e6 * a.e8 - a.e4 * a.e10,
    a.e4 * a.e9 - a.e8 * a.e5,
    0,
    a.e2 * a.e9 - a.e10 * a.e1,
    a.e10 * a.e0 - a.e2 * a.e8,
    a.e8 * a.e1 - a.e0 * a.e9,
    0,
    a.e6 * a.e1 - a.e2 * a.e5,
    a.e4 * a.e2 - a.e6 * a.e0,
    a.e0 * a.e5 - a.e4 * a.e1,
    0
  )

  def transformPoint(t: AffineMatrix, p: Vec) = new Vec(
    t.e0 * p.x + t.e1 * p.y + t.e2  * p.z + t.e3,
    t.e4 * p.x + t.e5 * p.y + t.e6  * p.z + t.e7,
    t.e8 * p.x + t.e9 * p.y + t.e10 * p.z + t.e11
  )

  def transformPoints(t: AffineMatrix, ps: List[Vec]) = ps.map { p => transformPoint(t, p) }
  def averagePoints(ps: List[Vec]) = {
    val avg = new Vec(0, 0, 0)
    for (p <- ps) {
      avg.x += p.x
      avg.y += p.y
      avg.z += p.z
    }
    val f = 1.0 / ps.length
    avg.x *= f
    avg.y *= f
    avg.z *= f
    avg
  }
}
