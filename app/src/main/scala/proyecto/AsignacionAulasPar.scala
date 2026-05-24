package proyecto

import common._
import AsignacionAulas._

object AsignacionAulasPar {

  /** Versión paralela de choques: divide el vector de cursos en dos mitades. */
  def choquesPar(cursos: Cursos, a: Asignacion): Int = {
    val n   = cursos.length
    val mid = n / 2
    val izq = (0 until mid).toVector
    val der = (mid until n).toVector

    // Choques dentro de un subconjunto de índices
    def choquesLocal(idxs: Vector[Int]): Int =
      idxs.flatMap { i =>
        idxs.filter { j =>
          j > i && a(i) == a(j) && a(i) >= 0 && solapan(cursos(i), cursos(j))
        }
      }.length

    // Pares que cruzan las dos mitades (no paralelizados)
    def choquesCruzados(): Int =
      izq.flatMap { i =>
        der.filter { j =>
          a(i) == a(j) && a(i) >= 0 && solapan(cursos(i), cursos(j))
        }
      }.length

    val (cl, cr) = parallel(choquesLocal(izq), choquesLocal(der))
    cl + cr + choquesCruzados()
  }

  /** Versión paralela de desperdicio: divide el vector de cursos en dos mitades. */
  def desperdicioPar(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = {
    val mid = cursos.length / 2

    def desperdicioRango(desde: Int, hasta: Int): Int =
      (desde until hasta).toVector
        .filter(i => a(i) >= 0 && capAula(aulas(a(i))) >= estCurso(cursos(i)))
        .map(i => capAula(aulas(a(i))) - estCurso(cursos(i)))
        .sum

    val (izq, der) = parallel(
      desperdicioRango(0, mid),
      desperdicioRango(mid, cursos.length)
    )
    izq + der
  }
  /** Versión paralela de movilidad: divide el vector de cursos en dos mitades. */
  def movilidadPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                   a: Asignacion): Int = {
    val asignados = cursos.indices.toVector
      .filter(i => a(i) >= 0)
      .sortBy(i => iniCurso(cursos(i)))

    val numPares = asignados.length - 1
    if (numPares <= 0) 0
    else {
      val mid = numPares / 2

      // Suma de distancias para los pares (desde, desde+1), …, (hasta-1, hasta)
      def sumaPares(desde: Int, hasta: Int): Int = {
        def go(i: Int): Int =
          if (i >= hasta) 0
          else d(a(asignados(i)))(a(asignados(i + 1))) + go(i + 1)
        go(desde)
      }

      val (izq, der) = parallel(sumaPares(0, mid), sumaPares(mid, numPares))
      izq + der
    }
  }

  /**
   * Versión paralela de generarAsignaciones:
   * paraleliza la construcción usando parallel sobre los valores del primer curso.
   */
  def generarAsignacionesPar(n: Int, m: Int): Vector[Asignacion] ={
    if (n == 0) Vector(Vector.empty[Int])
    else {
      val subAsignaciones = generarAsignaciones(n - 1, m)
      val mid = m / 2

      val (izq, der) = parallel(
        (0 until mid).toVector.flatMap { aulaIdx =>
          subAsignaciones.map(sub => aulaIdx +: sub)
        },
        (mid until m).toVector.flatMap { aulaIdx =>
          subAsignaciones.map(sub => aulaIdx +: sub)
        }
      )
      izq ++ der
    }
  }
  /**
   * Versión paralela de asignacionOptima:
   * divide el espacio de candidatos en dos mitades y combina los mínimos.
   */
  def asignacionOptimaPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                          w: Pesos): (Asignacion, Int) = {
    val candidatas = generarAsignacionesPar(cursos.length, aulas.length)
    val mid        = candidatas.length / 2
    val izq        = candidatas.take(mid)
    val der        = candidatas.drop(mid)

    // Mínimo local dentro de un subconjunto de candidatas (recursión de cola)
    def minimoLocal(cs: Vector[Asignacion]): (Asignacion, Int) = {
      // FIX: Guard against empty vectors created by uneven splits
      if (cs.isEmpty) {
        (Vector.empty[Int], Int.MaxValue)
      } else {
        def buscar(rest: Vector[Asignacion],
                   mejorAsig: Asignacion,
                   mejorCosto: Int): (Asignacion, Int) =
          rest match {
            case Vector() => (mejorAsig, mejorCosto)
            case _ =>
              val costo = costoAsignacion(cursos, aulas, d, rest.head, w)
              if (costo < mejorCosto) buscar(rest.tail, rest.head, costo)
              else                    buscar(rest.tail, mejorAsig, mejorCosto)
          }
        val primera = cs.head
        buscar(cs.tail, primera, costoAsignacion(cursos, aulas, d, primera, w))
      }
    }

    val (minIzq, minDer) = parallel(minimoLocal(izq), minimoLocal(der))
    if (minIzq._2 <= minDer._2) minIzq else minDer
  }
}
