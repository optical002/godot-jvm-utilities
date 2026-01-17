package io.github.optical002.godot.parser.core

enum Variant {
  // Primitives (6 types)
  case Nil
  case Bool(value: Boolean)
  case Int(value: Long)
  case Float(value: Double)
  case String(value: String)
  case StringName(value: String)

  // Math - 2D (4 types)
  case Vector2(x: Double, y: Double)
  case Vector2i(x: Long, y: Long)
  case Rect2(x: Double, y: Double, width: Double, height: Double)
  case Rect2i(x: Long, y: Long, width: Long, height: Long)

  // Math - 3D and transforms (7 types)
  case Vector3(x: Double, y: Double, z: Double)
  case Vector3i(x: Long, y: Long, z: Long)
  case Transform2D(xx: Double, xy: Double, yx: Double, yy: Double, ox: Double, oy: Double)
  case Plane(a: Double, b: Double, c: Double, d: Double)
  case Quaternion(x: Double, y: Double, z: Double, w: Double)
  case AABB(px: Double, py: Double, pz: Double, sx: Double, sy: Double, sz: Double)
  case Basis(xx: Double, xy: Double, xz: Double, yx: Double, yy: Double, yz: Double, zx: Double, zy: Double, zz: Double)

  // Math - 4D and advanced transforms (4 types)
  case Vector4(x: Double, y: Double, z: Double, w: Double)
  case Vector4i(x: Long, y: Long, z: Long, w: Long)
  case Transform3D(
    bxx: Double,
    bxy: Double,
    bxz: Double,
    byx: Double,
    byy: Double,
    byz: Double,
    bzx: Double,
    bzy: Double,
    bzz: Double,
    ox: Double,
    oy: Double,
    oz: Double
  )
  case Projection(
    xx: Double,
    xy: Double,
    xz: Double,
    xw: Double,
    yx: Double,
    yy: Double,
    yz: Double,
    yw: Double,
    zx: Double,
    zy: Double,
    zz: Double,
    zw: Double,
    wx: Double,
    wy: Double,
    wz: Double,
    ww: Double
  )

  // Color (1 type)
  case Color(r: Double, g: Double, b: Double, a: Double)

  // Path and RID (2 types)
  case NodePath(path: java.lang.String)
  case RID(id: Long)

  // Object (1 type with variants)
  case Object(value: ObjectValue)

  // Callable & Signal (2 types)
  case Callable(target: Option[Variant], method: java.lang.String)
  case Signal(source: Option[Variant], signalName: java.lang.String)

  // Collections (2 types)
  case Dictionary(entries: Map[java.lang.String, Variant], typed: Option[DictionaryType])
  case Array(elements: Vector[Variant], typed: Option[ArrayType])

  // Packed arrays (10 types)
  case PackedByteArray(data: Vector[Byte])
  case PackedInt32Array(data: Vector[scala.Int])
  case PackedInt64Array(data: Vector[Long])
  case PackedFloat32Array(data: Vector[scala.Float])
  case PackedFloat64Array(data: Vector[Double])
  case PackedStringArray(data: Vector[java.lang.String])
  case PackedVector2Array(data: Vector[(Double, Double)])
  case PackedVector3Array(data: Vector[(Double, Double, Double)])
  case PackedColorArray(data: Vector[(Double, Double, Double, Double)])
  case PackedVector4Array(data: Vector[(Double, Double, Double, Double)])
}

// Object reference types
enum ObjectValue {
  case ExtResource(id: java.lang.String)
  case SubResource(id: java.lang.String)
  case Resource(path: java.lang.String)
  case Null
}

// Typed collection metadata
case class ArrayType(
  builtinType: java.lang.String,
  className: java.lang.String,
  script: Option[Variant]
)

case class DictionaryType(
  keyBuiltinType: java.lang.String,
  keyClassName: java.lang.String,
  keyScript: Option[Variant],
  valueBuiltinType: java.lang.String,
  valueClassName: java.lang.String,
  valueScript: Option[Variant]
)

// Extension methods for type extraction
extension (v: Variant)
  def asString: Option[java.lang.String] = v match {
    case Variant.String(s) => Some(s)
    case _ => None
  }

  def asStringName: Option[java.lang.String] = v match {
    case Variant.StringName(s) => Some(s)
    case _ => None
  }

  def asInt: Option[Long] = v match {
    case Variant.Int(i) => Some(i)
    case _ => None
  }

  def asFloat: Option[Double] = v match {
    case Variant.Int(i) => Some(i)
    case Variant.Float(f) => Some(f)
    case _ => None
  }

  def asBool: Option[Boolean] = v match {
    case Variant.Bool(b) => Some(b)
    case _ => None
  }

  def asVector2: Option[(Double, Double)] = v match {
    case Variant.Vector2(x, y) => Some((x, y))
    case _ => None
  }

  def asVector3: Option[(Double, Double, Double)] = v match {
    case Variant.Vector3(x, y, z) => Some((x, y, z))
    case _ => None
  }

  def asColor: Option[(Double, Double, Double, Double)] = v match {
    case Variant.Color(r, g, b, a) => Some((r, g, b, a))
    case _ => None
  }

  def asNodePath: Option[java.lang.String] = v match {
    case Variant.NodePath(path) => Some(path)
    case _ => None
  }

  def asArray: Option[Vector[Variant]] = v match {
    case Variant.Array(elements, _) => Some(elements)
    case _ => None
  }

  def asDictionary: Option[Map[java.lang.String, Variant]] = v match {
    case Variant.Dictionary(entries, _) => Some(entries)
    case _ => None
  }

  def asObject: Option[ObjectValue] = v match {
    case Variant.Object(obj) => Some(obj)
    case _ => None
  }

  def asExtResource: Option[java.lang.String] = v match {
    case Variant.Object(ObjectValue.ExtResource(id)) => Some(id)
    case _ => None
  }

  def asSubResource: Option[java.lang.String] = v match {
    case Variant.Object(ObjectValue.SubResource(id)) => Some(id)
    case _ => None
  }

  // For use as Dictionary keys
  def asKey: Any = v match {
    case Variant.String(s) => s
    case Variant.StringName(s) => s
    case Variant.Int(i) => i
    case Variant.Float(f) => f
    case Variant.Bool(b) => b
    case Variant.Nil => "null"
    case other => other.toString
  }
