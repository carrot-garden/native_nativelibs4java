/*
 * ScalaCL2.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package scalacl
import com.nativelibs4java.opencl.OpenCL4Java._

//case class Dim(size: Int) extends Int1(size)


object ScalaCL {
  //implicit def Dim2Int(v: Dim) = v.size

  implicit def Int2Int1(v: Int) = Int1(v)
  implicit def Int12Int(v: Int1) = v.value
  implicit def Double2Double1(v: Double) = Double1(v)
  implicit def Double12Double(v: Double1) = v.value

  def local[V <: AbstractVar](v: V): V = { v.scope = LocalScope; v }
  def global[V <: AbstractVar](v: V): V = { v.scope = GlobalScope; v }
  def priv[V <: AbstractVar](v: V): V = { v.scope = PrivateScope; v }

  def cos(x: Expr) = Fun("cos", DoubleType, x)
  def sin(x: Expr) = Fun("sin", DoubleType, x)
  def tan(x: Expr) = Fun("tan", DoubleType, x)
  def atan(x: Expr) = Fun("atan", DoubleType, x)
  def acos(x: Expr) = Fun("acos", DoubleType, x)
  def asin(x: Expr) = Fun("asin", DoubleType, x)
  def cosh(x: Expr) = Fun("cosh", DoubleType, x)
  def sinh(x: Expr) = Fun("sinh", DoubleType, x)
  def tanh(x: Expr) = Fun("tanh", DoubleType, x)
  def atan2(x: Expr, y: Expr) = Fun("atan2", DoubleType, x, y);

  implicit def Expr2Stat(expr: Expr) = ExprStat(expr)
}