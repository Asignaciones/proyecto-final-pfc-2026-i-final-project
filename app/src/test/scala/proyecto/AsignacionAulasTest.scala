package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasTest extends AnyFunSuite {

  // Ejemplo 1 del enunciado
  val c1: Cursos    = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas     = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos      = (1000, 100, 1, 2)

  // solapan
  test("solapan: solapamiento parcial al final del primero") {
    // [0,6) y [4,10) se solapan en [4,6)
    assert(solapan(("A", 0, 6, 10), ("B", 4, 10, 10)))
  }

  test("solapan: un curso contenido completamente dentro de otro") {
    // [2,10) contiene a [4,6) — deben solaparse
    assert(solapan(("X", 2, 10, 15), ("Y", 4, 6, 10)))
  }

  test("solapan: mismo horario exacto se solapa") {
    // [4,8) y [4,8) — idénticos, se solapan
    assert(solapan(("A", 4, 8, 20), ("B", 4, 8, 20)))
  }

  test("solapan: cursos sin contacto con brecha entre ellos no se solapan") {
    // [0,4) y [6,10) — hay brecha de [4,6), no se solapan
    assert(!solapan(("A", 0, 4, 10), ("B", 6, 10, 10)))
  }

  test("solapan: segundo curso empieza justo cuando el primero termina — no se solapan") {
    // [6,10) termina en 10, [10,14) empieza en 10 — adyacentes sin intersección
    assert(!solapan(("A", 6, 10, 25), ("B", 10, 14, 30)))
  }

  // choques
  test("choques: todos en la misma aula con solapamiento múltiple") {
    // M01[4,8), M02[6,10), M03[5,7) — los 3 en aula 0, se solapan entre sí
    val cursos = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 5, 7, 20))
    assert(choques(cursos, Vector(0, 0, 0)) == 3)
  }

  test("choques: misma aula pero sin solapamiento") {
    // M01[0,4) y M02[4,8) — adyacentes, no se solapan
    val cursos = Vector(("M01", 0, 4, 25), ("M02", 4, 8, 30))
    assert(choques(cursos, Vector(0, 0)) == 0)
  }

  test("choques: solapamiento pero en aulas distintas no cuenta") {
    // M01[4,8) y M02[6,10) se solapan pero están en aulas diferentes
    assert(choques(c1, Vector(0, 1, 1)) == 0)
  }

  test("choques: un solo curso no puede generar choques") {
    val cursos = Vector(("M01", 4, 8, 25))
    assert(choques(cursos, Vector(0)) == 0)
  }

  test("choques: cuatro cursos, dos pares en conflicto") {
    // [0,6) y [4,8) en aula 0 se solapan — [2,5) y [3,7) en aula 1 se solapan
    val cursos = Vector(("A", 0, 6, 10), ("B", 4, 8, 10), ("C", 2, 5, 10), ("D", 3, 7, 10))
    assert(choques(cursos, Vector(0, 0, 1, 1)) == 2)
  }

  // capacidadFallida
  test("capacidadFallida: asignacion [0,0,1] no falla capacidad") {
    assert(capacidadFallida(c1, a1, Vector(0, 0, 1)) == 0)
  }

  // desperdicio
  test("desperdicio: asignacion [0,0,1] tiene desperdicio 25") {
    // E101(30)-M01(25)=5, E101(30)-M02(30)=0, E102(40)-M03(20)=20 → 25
    assert(desperdicio(c1, a1, Vector(0, 0, 1)) == 25)
  }

  test("desperdicio: asignacion [0,1,0] tiene desperdicio 25") {
    // E101(30)-M01(25)=5, E102(40)-M02(30)=10, E101(30)-M03(20)=10 → 25
    assert(desperdicio(c1, a1, Vector(0, 1, 0)) == 25)
  }

  // costoAsignacion
  test("costoAsignacion: asignacion [0,0,1] cuesta 1031") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w) == 1031)
  }

  test("costoAsignacion: asignacion [0,1,0] cuesta 37") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 1, 0), w) == 37)
  }

  // generarAsignaciones
  test("generarAsignaciones: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignaciones(2, 2).length == 4)
  }

  test("generarAsignaciones: 3 cursos y 3 aulas produce 27 asignaciones") {
    assert(generarAsignaciones(3, 3).length == 27)
  }

  // asignacionOptima
  test("asignacionOptima: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costo <= 37)
  }
}
