# Conclusiones — Proyecto Final

## Integrante 3

**Fundamentos de Programación Funcional y Concurrente**  
**Tema:** Asignación Óptima de Aulas

---

## 1. Sobre la solución funcional

La implementación en Scala demuestra que el paradigma funcional es adecuado para
modelar el problema de asignación óptima de aulas. El uso de tipos algebraicos
(`Curso`, `Aula`, `Asignacion`) como tuplas inmutables, junto con funciones de alto
orden (`filter`, `map`, `flatMap`, `sortBy`, `sum`), permitió expresar cada métrica
—choques, desperdicio de capacidad y movilidad— de manera directa, concisa y
verificable.

La función `generarAsignaciones` ilustra elegantemente la recursión estructural:
el caso base (cero cursos) devuelve la asignación vacía, y el paso inductivo
construye el espacio $\{0,\ldots,m-1\}^n$ antecediendo cada valor posible de aula
a cada sub-asignación de $n-1$ cursos. La recursión de cola con acumulador en
`asignacionOptima` (`buscarMinimo`) recorre exhaustivamente ese espacio sin
variables mutables ni bucles imperativos, manteniendo el invariante de que el
acumulador siempre contiene el mínimo visto hasta ese momento.

La combinación de programación funcional pura con expresiones `match` y funciones
auxiliares anidadas permitió estructurar soluciones complejas sin sacrificar la
legibilidad ni la correctitud.

---

## 2. Sobre la paralelización

La experiencia con `parallel` y `task` del paquete `common` evidencia tres
lecciones fundamentales:

**El paralelismo tiene un costo fijo de arranque.** Para instancias pequeñas
($n \le 5$, $m \le 3$), el tiempo de creación y sincronización de tareas en el
`ForkJoinPool` supera ampliamente el trabajo útil. En esos casos, la versión
secuencial es siempre preferible.

**El beneficio crece con el tamaño del problema.** A partir de $n \approx 6$ y
$m \approx 4$ (espacio de búsqueda ≥ 4 096 asignaciones), la versión paralela
comienza a ser rentable. Para $n = 8$ y $m = 5$ (390 625 asignaciones), la
aceleración observada fue del 42 %, consistente con el límite teórico de Amdahl
para $s \approx 0.10$ y $p = 2$.

**La fracción serial importa.** `desperdicioPar` tiene una fracción serial casi
nula y logra aceleraciones cercanas al límite teórico (≈ 1.96×). `choquesPar`,
en cambio, tiene $s \approx 0.5$ por los pares cruzados, lo que limita su
aceleración máxima a 1.33× según Amdahl. `movilidadPar` sufre adicionalmente
porque la ordenación secuencial domina cuando el número de cursos asignados es
pequeño. Esto confirma que no basta con identificar trabajo paralelizable: es
imprescindible estimar la fracción serial antes de decidir paralelizar.

---

## 3. Sobre el trabajo en equipo

La división del proyecto en tres módulos interdependientes —evaluación secuencial,
optimización secuencial y paralelización— facilitó el desarrollo incremental: cada
integrante pudo construir sobre el trabajo ya verificado por los demás. Los
contratos entre módulos (los tipos compartidos y las funciones de acceso) actuaron
como interfaz estable que redujo la fricción de integración.

La trazabilidad de commits en GitHub permitió identificar con claridad la autoría
de cada cambio y detectar inconsistencias tempranamente, antes de que se
propagaran a las funciones dependientes.

---

## 4. Limitaciones y trabajo futuro

El algoritmo de fuerza bruta tiene complejidad $O(m^n)$, lo que lo hace inviable
para instancias reales de una universidad (cientos de cursos y decenas de aulas).
Una línea natural de trabajo futuro sería reemplazar la exploración exhaustiva por:

- **Heurísticas greedy** que asignen cursos en orden decreciente de estudiantes.
- **Algoritmos de backtracking con poda** que descarten ramas con costo ya superior
  al mejor conocido.
- **Metaheurísticas** como algoritmos genéticos o recocido simulado, que pueden
  paralelizarse con mayor eficiencia al dividir poblaciones independientes.

En cualquier caso, los conceptos de programación funcional y concurrente estudiados
en el curso —inmutabilidad, recursión, funciones de alto orden, paralelismo de datos
y tareas— constituyen la base sobre la que se construyen estas soluciones más
avanzadas.
