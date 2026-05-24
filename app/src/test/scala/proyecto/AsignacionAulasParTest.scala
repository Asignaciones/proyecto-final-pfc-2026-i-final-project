package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._
import AsignacionAulasPar._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasParTest extends AnyFunSuite {

  val c1: Cursos = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos = (1000, 100, 1, 2)

  // Ejemplo 2: 4 cursos, 2 aulas (F03 no cabe en ninguna)
  val c2: Cursos = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
  val a2: Aulas = Vector(("S201", 45), ("S202", 30))
  val d2: Distancias = Vector(Vector(0, 5), Vector(5, 0))

  test("choquesPar: asignacion [0,0,1] tiene 1 choque") {
    assert(choquesPar(c1, Vector(0, 0, 1)) == 1)
  }

  test("choquesPar: asignacion [0,1,0] no tiene choques") {
    assert(choquesPar(c1, Vector(0, 1, 0)) == 0)
  }

  test("choquesPar: resultado identico al secuencial para [0,0,1]") {
    val asig = Vector(0, 0, 1)
    assert(choquesPar(c1, asig) == choques(c1, asig))
  }

  test("choquesPar: resultado identico al secuencial para [0,1,1]") {
    val asig = Vector(0, 1, 1)
    assert(choquesPar(c1, asig) == choques(c1, asig))
  }

  test("choquesPar: tres cursos solapados en la misma aula producen 3 choques") {
    val cursos = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 5, 7, 20))
    assert(choquesPar(cursos, Vector(0, 0, 0)) == 3)
  }

  test("choquesPar: solapamiento en aulas distintas no cuenta") {
    assert(choquesPar(c1, Vector(0, 1, 1)) == 0)
  }

  test("choquesPar: cuatro cursos con dos pares en conflicto") {
    val cursos = Vector(("A", 0, 6, 10), ("B", 4, 8, 10), ("C", 2, 5, 10), ("D", 3, 7, 10))
    assert(choquesPar(cursos, Vector(0, 0, 1, 1)) == 2)
  }

  test("choquesPar: resultado identico al secuencial en ejemplo 2 asignacion [0,1,0,1]") {
    val asig = Vector(0, 1, 0, 1)
    assert(choquesPar(c2, asig) == choques(c2, asig))
  }

  test("desperdicioPar: asignacion [0,0,1] tiene desperdicio 25") {
    assert(desperdicioPar(c1, a1, Vector(0, 0, 1)) == 25)
  }

  test("desperdicioPar: resultado identico al secuencial para [0,1,0]") {
    val asig = Vector(0, 1, 0)
    assert(desperdicioPar(c1, a1, asig) == desperdicio(c1, a1, asig))
  }

  test("desperdicioPar: aula exactamente del tamano del curso no desperdicia") {
    val cursos = Vector(("A", 0, 4, 30))
    val aulas = Vector(("E101", 30))
    assert(desperdicioPar(cursos, aulas, Vector(0)) == 0)
  }

  test("desperdicioPar: aula insuficiente no suma al desperdicio") {
    val cursos = Vector(("F03", 8, 12, 50))
    val aulas = Vector(("S201", 45))
    assert(desperdicioPar(cursos, aulas, Vector(0)) == 0)
  }

  test("desperdicioPar: resultado identico al secuencial en ejemplo 2 [0,1,0,1]") {
    val asig = Vector(0, 1, 0, 1)
    assert(desperdicioPar(c2, a2, asig) == desperdicio(c2, a2, asig))
  }

  test("desperdicioPar: varios cursos todos con sobrante suman 60") {
    val cursos = Vector(("A", 0, 4, 10), ("B", 4, 8, 20), ("C", 8, 12, 30))
    val aulas = Vector(("E101", 40))
    assert(desperdicioPar(cursos, aulas, Vector(0, 0, 0)) == 60)
  }

  test("desperdicioPar: un curso con mucho sobrante devuelve 45") {
    val cursos = Vector(("A", 0, 4, 5))
    val aulas = Vector(("E101", 50))
    assert(desperdicioPar(cursos, aulas, Vector(0)) == 45)
  }

  test("desperdicioPar: resultado identico al secuencial para vector de 6 cursos") {
    val cursos = Vector(("A", 0, 3, 10), ("B", 3, 6, 20), ("C", 6, 9, 30),
      ("D", 9, 12, 10), ("E", 12, 15, 20), ("F", 15, 18, 30))
    val aulas = Vector(("E1", 35), ("E2", 50))
    val asig = Vector(0, 1, 0, 1, 0, 1)
    assert(desperdicioPar(cursos, aulas, asig) == desperdicio(cursos, aulas, asig))
  }

  test("movilidadPar: asignacion [0,0,1] tiene movilidad 3") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  test("movilidadPar: [0,1,0] tiene movilidad 6") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 1, 0)) == 6)
  }

  test("movilidadPar: resultado identico al secuencial para [0,0,1]") {
    val asig = Vector(0, 0, 1)
    assert(movilidadPar(c1, a1, d1, asig) == movilidad(c1, a1, d1, asig))
  }

  test("movilidadPar: todos en la misma aula tiene movilidad 0") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 0)) == 0)
  }

  test("movilidadPar: ejemplo 2 asignacion [0,1,0,1] tiene movilidad 15") {
    assert(movilidadPar(c2, a2, d2, Vector(0, 1, 0, 1)) == 15)
  }

  test("movilidadPar: resultado identico al secuencial en ejemplo 2 [0,1,0,1]") {
    val asig = Vector(0, 1, 0, 1)
    assert(movilidadPar(c2, a2, d2, asig) == movilidad(c2, a2, d2, asig))
  }

  test("movilidadPar: un solo curso asignado tiene movilidad 0") {
    assert(movilidadPar(c1, a1, d1, Vector(0, -1, -1)) == 0)
  }

  test("movilidadPar: resultado identico al secuencial para [1,0,1]") {
    val asig = Vector(1, 0, 1)
    assert(movilidadPar(c1, a1, d1, asig) == movilidad(c1, a1, d1, asig))
  }

  test("generarAsignacionesPar: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignacionesPar(2, 2).length == 4)
  }

  test("generarAsignacionesPar: 3 cursos y 3 aulas produce 27 asignaciones") {
    assert(generarAsignacionesPar(3, 3).length == 27)
  }

  test("generarAsignacionesPar: contiene exactamente las mismas asignaciones que la version secuencial") {
    val par = generarAsignacionesPar(3, 2).sortBy(_.mkString)
    val seq = generarAsignaciones(3, 2).sortBy(_.mkString)
    assert(par == seq)
  }

  test("generarAsignacionesPar: cada asignacion tiene exactamente n elementos") {
    assert(generarAsignacionesPar(4, 3).forall(_.length == 4))
  }

  test("generarAsignacionesPar: todas las combinaciones de 2 cursos 2 aulas estan presentes") {
    val result = generarAsignacionesPar(2, 2)
    assert(result.contains(Vector(0, 0)))
    assert(result.contains(Vector(0, 1)))
    assert(result.contains(Vector(1, 0)))
    assert(result.contains(Vector(1, 1)))
  }

  test("generarAsignacionesPar: 0 cursos produce exactamente una asignacion vacia") {
    val r = generarAsignacionesPar(0, 3)
    assert(r.length == 1 && r.head.isEmpty)
  }

  test("generarAsignacionesPar: 4 cursos 5 aulas produce 625 asignaciones") {
    assert(generarAsignacionesPar(4, 5).length == 625)
  }

  test("generarAsignacionesPar: resultado identico al secuencial para (4, 3)") {
    val par = generarAsignacionesPar(4, 3).sortBy(_.mkString)
    val seq = generarAsignaciones(4, 3).sortBy(_.mkString)
    assert(par == seq)
  }

  test("asignacionOptimaPar: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costo <= 37)
  }
  test("asignacionOptimaPar: la asignacion tiene exactamente n elementos") {
    val (asig, _) = asignacionOptimaPar(c1, a1, d1, w)
    assert(asig.length == c1.length)
  }

  test("asignacionOptimaPar: el costo reportado coincide con costoAsignacion de la asignacion devuelta") {
    val (asig, costo) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costoAsignacion(c1, a1, d1, asig, w) == costo)
  }

  test("asignacionOptimaPar: produce el mismo costo optimo que la version secuencial — ejemplo 1") {
    val (_, costoSec) = asignacionOptima(c1, a1, d1, w)
    val (_, costoPar) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costoPar == costoSec)
  }

  test("asignacionOptimaPar: todos los indices de aula son validos") {
    val (asig, _) = asignacionOptimaPar(c1, a1, d1, w)
    assert(asig.forall(j => j >= 0 && j < a1.length))
  }

  test("asignacionOptimaPar: un curso una aula devuelve [0]") {
    val cursos = Vector(("A", 0, 4, 20))
    val aulas  = Vector(("E101", 30))
    val dist   = Vector(Vector(0))
    val (asig, _) = asignacionOptimaPar(cursos, aulas, dist, w)
    assert(asig == Vector(0))
  }

  test("asignacionOptimaPar: produce el mismo costo optimo que la version secuencial — ejemplo 2") {
    val (_, costoSec) = asignacionOptima(c2, a2, d2, w)
    val (_, costoPar) = asignacionOptimaPar(c2, a2, d2, w)
    assert(costoPar == costoSec)
  }
}
