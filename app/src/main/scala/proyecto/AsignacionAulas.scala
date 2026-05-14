package proyecto

import scala.util.Random

object AsignacionAulas {

  // Un curso es (id, horaInicio, horaFin, numEstudiantes).
  // Las horas son bloques de 30 min desde las 6:00 a.m. (p. ej. ini=4 → 8:00 a.m.).
  type Curso = (String, Int, Int, Int)
  type Cursos = Vector[Curso]

  // Un aula es (id, capacidad).
  type Aula = (String, Int)
  type Aulas = Vector[Aula]

  // Asignacion(i) = j significa que el curso i se dicta en el aula j; -1 = sin asignar.
  type Asignacion = Vector[Int]

  // Matriz simétrica de distancias entre aulas.
  type Distancias = Vector[Vector[Int]]

  // Pesos: (w_CH, w_CF, w_DE, w_MV).
  type Pesos = (Int, Int, Int, Int)

  // ---------------------------------------------------------------------------
  // Funciones de generación (ya implementadas — NO MODIFICAR)
  // ---------------------------------------------------------------------------

  val random = new Random()

  def cursosAlAzar(n: Int): Cursos =
    Vector.tabulate(n) { i =>
      val ini = random.nextInt(29)
      val dur = random.nextInt(7) + 2
      ("C" + i, ini, ini + dur, random.nextInt(46) + 5)
    }

  def aulasAlAzar(m: Int): Aulas =
    Vector.tabulate(m)(j => ("E" + j, random.nextInt(46) + 15))

  def distanciasAlAzar(m: Int): Distancias = {
    val v = Vector.fill(m, m)(random.nextInt(m * 2) + 1)
    Vector.tabulate(m, m)((i, j) =>
      if (i < j) v(i)(j)
      else if (i == j) 0
      else v(j)(i))
  }

  // ---------------------------------------------------------------------------
  // Funciones de acceso (ya implementadas — NO MODIFICAR)
  // ---------------------------------------------------------------------------

  def idCurso(c: Curso): String = c._1
  def iniCurso(c: Curso): Int   = c._2
  def finCurso(c: Curso): Int   = c._3
  def estCurso(c: Curso): Int   = c._4

  def idAula(a: Aula): String   = a._1
  def capAula(a: Aula): Int     = a._2

  // ---------------------------------------------------------------------------
  // Funciones a implementar
  // ---------------------------------------------------------------------------

  /** Devuelve true sii los intervalos [ini1, fin1) y [ini2, fin2) se traslapan. */
  // AsignacionAulas.scala
  def solapan(c1: Curso, c2: Curso): Boolean = {
    iniCurso(c1) < finCurso(c2) && iniCurso(c2) < finCurso(c1)
  }

  /**
   * Número de pares (i, j) con i < j tales que a(i) == a(j) >= 0
   * y los cursos i y j se solapan.
   */
  def choques(cursos: Cursos, a: Asignacion): Int = ???

  /** Cantidad de cursos cuya aula asignada tiene capacidad menor al número de estudiantes. */
  def capacidadFallida(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = ???

  /**
   * Suma de (cap(aula_i) - est(curso_i)) para los cursos asignados
   * con capacidad suficiente.
   */
  def desperdicio(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = ???

  /**
   * Ordena los cursos asignados por hora de inicio y suma las distancias
   * entre aulas de cursos consecutivos.
   */
  def movilidad(cursos: Cursos, aulas: Aulas, d: Distancias,
                a: Asignacion): Int = ???

  /** Costo total: w_CH * CH + w_CF * CF + w_DE * DE + w_MV * MV. */
  def costoAsignacion(cursos: Cursos, aulas: Aulas, d: Distancias,
                      a: Asignacion, w: Pesos): Int = ???

  /**
   * Genera todas las asignaciones completas posibles: vectores en {0,..,m-1}^n.
   * El tamaño del resultado es m^n.
   */
  def generarAsignaciones(n: Int, m: Int): Vector[Asignacion] = ???

  /**
   * Devuelve la asignación de mínimo costo y su costo.
   * Usa generarAsignaciones para explorar el espacio.
   */
  def asignacionOptima(cursos: Cursos, aulas: Aulas, d: Distancias,
                       w: Pesos): (Asignacion, Int) = ???
}
