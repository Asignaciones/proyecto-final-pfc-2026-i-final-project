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

  // Ejemplo 2 del enunciado: 4 cursos, 2 aulas (F03 no cabe en ninguna)
  val c2: Cursos     = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
  val a2: Aulas      = Vector(("S201", 45), ("S202", 30))
  val d2: Distancias = Vector(Vector(0, 5), Vector(5, 0))

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
  test("capacidadFallida: todas las aulas con capacidad suficiente") {
    // E101(30) >= M01(25), E102(40) >= M02(30), E101(30) >= M03(20)
    assert(capacidadFallida(c1, a1, Vector(0, 1, 0)) == 0)
  }

  test("capacidadFallida: una aula insuficiente") {
    // E101(30) < M02(30) es suficiente, pero E101(30) < 35 estudiantes falla
    val cursos = Vector(("X", 0, 4, 35), ("Y", 4, 8, 20))
    val aulas  = Vector(("E101", 30), ("E102", 40))
    assert(capacidadFallida(cursos, aulas, Vector(0, 0)) == 1)
  }

  test("capacidadFallida: todas las aulas insuficientes") {
    // aula de cap 10 no alcanza para ningún curso
    val cursos = Vector(("A", 0, 4, 25), ("B", 4, 8, 30))
    val aulas  = Vector(("E001", 10))
    assert(capacidadFallida(cursos, aulas, Vector(0, 0)) == 2)
  }

  test("capacidadFallida: capacidad exactamente igual no falla") {
    // cap == est → no es fallo
    val cursos = Vector(("A", 0, 4, 30))
    val aulas  = Vector(("E101", 30))
    assert(capacidadFallida(cursos, aulas, Vector(0)) == 0)
  }

  test("capacidadFallida: ejemplo 2 del enunciado asignacion [0,1,0,1] tiene 1 fallo") {
    val c2 = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
    val a2 = Vector(("S201", 45), ("S202", 30))
    // F03(50) en S201(45) → falla
    assert(capacidadFallida(c2, a2, Vector(0, 1, 0, 1)) == 1)
  }

  // desperdicio
  test("desperdicio: aula exactamente del tamaño del curso no desperdicia") {
    val cursos = Vector(("A", 0, 4, 30))
    val aulas  = Vector(("E101", 30))
    assert(desperdicio(cursos, aulas, Vector(0)) == 0)
  }

  test("desperdicio: curso con aula insuficiente no suma al desperdicio") {
    // F03(50) en S201(45) — cap < est, no cuenta
    val c2 = Vector(("F03", 8, 12, 50))
    val a2 = Vector(("S201", 45))
    assert(desperdicio(c2, a2, Vector(0)) == 0)
  }

  test("desperdicio: ejemplo 2 del enunciado asignacion [0,1,0,1]") {
    val c2 = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
    val a2 = Vector(("S201", 45), ("S202", 30))
    // F01: 45-40=5, F02: 30-25=5, F03: falla cap no cuenta, F04: 30-15=15 → 25
    assert(desperdicio(c2, a2, Vector(0, 1, 0, 1)) == 25)
  }

  test("desperdicio: un solo curso con mucho espacio sobrante") {
    val cursos = Vector(("A", 0, 4, 5))
    val aulas  = Vector(("E101", 50))
    assert(desperdicio(cursos, aulas, Vector(0)) == 45)
  }

  test("desperdicio: varios cursos todos con sobrante") {
    val cursos = Vector(("A", 0, 4, 10), ("B", 4, 8, 20), ("C", 8, 12, 30))
    val aulas  = Vector(("E101", 40))
    // 40-10=30, 40-20=20, 40-30=10 → 60
    assert(desperdicio(cursos, aulas, Vector(0, 0, 0)) == 60)
  }

  //Movilidad
  test("movilidad: asignacion [0,0,1] — orden M01,M02,M03 — distancias D[0,0]+D[0,1]=0+3=3") {
    // Cursos ordenados por ini: M01(4), M02(6), M03(12)
    // aulas: 0,0,1 → D[0][0] + D[0][1] = 0 + 3 = 3
    assert(movilidad(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  test("movilidad: asignacion [0,1,0] — distancias D[0,1]+D[1,0]=3+3=6") {
    // M01(aula0)→M02(aula1)→M03(aula0): D[0][1]+D[1][0] = 3+3 = 6
    assert(movilidad(c1, a1, d1, Vector(0, 1, 0)) == 6)
  }

  test("movilidad: un solo curso asignado — movilidad 0") {
    // Sin pares consecutivos, no hay distancia que sumar
    assert(movilidad(c1, a1, d1, Vector(0, -1, -1)) == 0)
  }

  test("movilidad: todos en la misma aula — distancias 0") {
    // D[j][j] = 0 para todo j
    assert(movilidad(c1, a1, d1, Vector(0, 0, 0)) == 0)
  }

  test("movilidad: ejemplo 2 asignacion [0,1,0,1] — cursos consecutivos F01,F02,F03,F04") {
    // No se solapan, orden ini: F01(0),F02(4),F03(8),F04(12)
    // aulas: 0,1,0,1 → D[0][1]+D[1][0]+D[0][1] = 5+5+5 = 15
    assert(movilidad(c2, a2, d2, Vector(0, 1, 0, 1)) == 15)
  }

  test("movilidad: orden por hora de inicio respetado independientemente del orden en el vector") {
    // C(ini=2), A(ini=0), B(ini=6) — deben ordenarse A,C,B
    // aulas: A→0, C→1, B→0 → D[0][1]+D[1][0] = 3+3 = 6
    val cursos = Vector(("C", 2, 4, 10), ("A", 0, 2, 10), ("B", 6, 8, 10))
    assert(movilidad(cursos, a1, d1, Vector(1, 0, 0)) == 6)
  }
  // costoAsignacion
  test("costoAsignacion: asignacion [0,0,1] cuesta 1031") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w) == 1031)
  }

  test("costoAsignacion: asignacion [0,1,0] cuesta 37") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 1, 0), w) == 37)
  }
  test("costoAsignacion: ejemplo 2 asignacion [0,1,0,1] cuesta 155 segun enunciado") {
    // CH=0, CF=1, DE=25, MV=15 → 0+100+25+30=155
    assert(costoAsignacion(c2, a2, d2, Vector(0, 1, 0, 1), w) == 155)
  }

  test("costoAsignacion: ejemplo 2 asignacion [0,1,1,0] cuesta 160 segun enunciado") {
    // CH=0, CF=1, DE=40, MV=10 → 0+100+40+20=160
    assert(costoAsignacion(c2, a2, d2, Vector(0, 1, 1, 0), w) == 160)
  }

  test("costoAsignacion: sin choques ni fallos — solo desperdicio y movilidad") {
    // Un solo curso, aula suficiente
    val cursos = Vector(("A", 0, 4, 10))
    val aulas  = Vector(("E101", 30))
    val dist   = Vector(Vector(0))
    // CH=0, CF=0, DE=20, MV=0 → 20
    assert(costoAsignacion(cursos, aulas, dist, Vector(0), w) == 20)
  }
  // generarAsignaciones
  test("generarAsignaciones: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignaciones(2, 2).length == 4)
  }

  test("generarAsignaciones: 3 cursos y 3 aulas produce 27 asignaciones") {
    assert(generarAsignaciones(3, 3).length == 27)
  }

  test("generarAsignaciones: 0 cursos produce exactamente una asignacion vacia") {
    val resultado = generarAsignaciones(0, 3)
    assert(resultado.length == 1)
    assert(resultado.head.isEmpty)
  }

  test("generarAsignaciones: 1 curso 3 aulas produce 3 asignaciones una por aula") {
    val resultado = generarAsignaciones(1, 3)
    assert(resultado.length == 3)
    assert(resultado.contains(Vector(0)))
    assert(resultado.contains(Vector(1)))
    assert(resultado.contains(Vector(2)))
  }

  test("generarAsignaciones: todas las asignaciones de 2 cursos 2 aulas son correctas") {
    val resultado = generarAsignaciones(2, 2)
    assert(resultado.contains(Vector(0, 0)))
    assert(resultado.contains(Vector(0, 1)))
    assert(resultado.contains(Vector(1, 0)))
    assert(resultado.contains(Vector(1, 1)))
  }

  test("generarAsignaciones: cada asignacion tiene exactamente n elementos") {
    val resultado = generarAsignaciones(4, 3)
    assert(resultado.forall(_.length == 4))
  }
  // asignacionOptima
  test("asignacionOptima: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costo <= 37)
  }
  test("asignacionOptima: la asignacion devuelta tiene exactamente n cursos") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(asig.length == c1.length)
  }

  test("asignacionOptima: todos los indices de aula son validos") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(asig.forall(j => j >= 0 && j < a1.length))
  }

  test("asignacionOptima: el costo reportado coincide con costoAsignacion de la asignacion devuelta") {
    val (asig, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costoAsignacion(c1, a1, d1, asig, w) == costo)
  }

  test("asignacionOptima: con un solo curso y una sola aula la optima es [0]") {
    val cursos = Vector(("A", 0, 4, 20))
    val aulas  = Vector(("E101", 30))
    val dist   = Vector(Vector(0))
    val (asig, _) = asignacionOptima(cursos, aulas, dist, w)
    assert(asig == Vector(0))
  }
}
