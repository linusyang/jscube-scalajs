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
object ShapeUtil {
  def rebuildMeta(shape: Shape) = {
    val vertices = shape.vertices
    for (qf <- shape.quads) {
      val vert0 = vertices(qf.v0)
      val vert1 = vertices(qf.v1)
      val vert2 = vertices(qf.v2)
      val vec01 = VectorUtil.subPoints3d(vert1, vert0)
      val vec02 = VectorUtil.subPoints3d(vert2, vert0)
      val n1 = VectorUtil.crossProduct(vec01, vec02)

      var n2 = new Vec(0, 0)
      var centroid = new Vec(0, 0)
      if (qf.isTriangle()) {
        n2 = n1
        centroid = MatrixUtil.averagePoints(List(vert0, vert1, vert2))
      } else {
        val vert3 = vertices(qf.v3)
        val vec03 = VectorUtil.subPoints3d(vert3, vert0)
        n2 = VectorUtil.crossProduct(vec02, vec03)
        centroid = MatrixUtil.averagePoints(List(vert0, vert1, vert2, vert3))
      }
      qf.centroid = centroid
      qf.normal1 = n1
      qf.normal2 = n2
    }
    shape
  }

  //     4 -- 0
  //    /|   /|     +y
  //   5 -- 1 |      |__ +x
  //   | 7 -|-3     /
  //   |/   |/    +z
  //   6 -- 2
  def makeBox(w: Double, h: Double, d: Double) = {
    val s = new Shape()
    s.vertices = List(
      new Vec( w,  h, -d),
      new Vec( w,  h,  d),
      new Vec( w, -h,  d),
      new Vec( w, -h, -d),
      new Vec(-w,  h, -d),
      new Vec(-w,  h,  d),
      new Vec(-w, -h,  d),
      new Vec(-w, -h, -d)
    )
    s.quads = List(
      new QuadFace(0, 1, 2, 3),
      new QuadFace(1, 5, 6, 2),
      new QuadFace(5, 4, 7, 6),
      new QuadFace(4, 0, 3, 7),
      new QuadFace(0, 4, 5, 1),
      new QuadFace(2, 6, 7, 3)
    )
    rebuildMeta(s)
  }

  // Cube
  def makeCube(whd: Double) = makeBox(whd, whd, whd)

  // Icosahedron
  def makeTwenty(w: Double) = {
    val X = w
    val Z = X * (Math.sqrt(5) + 1.0) / 2.0
    val s = new Shape()
    s.vertices = List(
      new Vec(-X, 0.0, Z),
      new Vec(X, 0.0, Z),
      new Vec(-X, 0.0, -Z),
      new Vec(X, 0.0, -Z),
      new Vec(0.0, Z, X),
      new Vec(0.0, Z, -X),
      new Vec(0.0, -Z, X),
      new Vec(0.0, -Z, -X),
      new Vec(Z, X, 0.0),
      new Vec(-Z, X, 0.0),
      new Vec(Z, -X, 0.0),
      new Vec(-Z, -X, 0.0)
    )
    s.quads = List(
      new QuadFace(1, 4, 0),
      new QuadFace(4, 9, 0),
      new QuadFace(4, 5, 9),
      new QuadFace(8, 5, 4),
      new QuadFace(1, 8, 4),
      new QuadFace(1, 10, 8),
      new QuadFace(10, 3, 8),
      new QuadFace(8, 3, 5),
      new QuadFace(3, 2, 5),
      new QuadFace(3, 7, 2),
      new QuadFace(3, 10, 7),
      new QuadFace(10, 6, 7),
      new QuadFace(6, 11, 7),
      new QuadFace(6, 0, 11),
      new QuadFace(6, 1, 0),
      new QuadFace(10, 1, 6),
      new QuadFace(11, 0, 9),
      new QuadFace(2, 11, 9),
      new QuadFace(5, 2, 9),
      new QuadFace(11, 2, 7)
    )
    rebuildMeta(s)
  }
}
