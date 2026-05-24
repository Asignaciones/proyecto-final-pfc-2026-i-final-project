# Informe de Corrección — Proyecto Final

**Fundamentos de Programación Funcional y Concurrente**  
**Tema:** Asignación Óptima de Aulas

---

## Integrante 1 — `solapan`, `choques`, `capacidadFallida`, `desperdicio`

---

### Corrección de `solapan`

**Especificación:** dados dos cursos $c_1$ y $c_2$, la función debe devolver `true` si y
solo si sus intervalos de tiempo tienen intersección no vacía:

$$
\text{solapan}(c_1, c_2) = \text{true} \iff \text{ini}_{c_1} < \text{fin}_{c_2} \;\land\; \text{ini}_{c_2} < \text{fin}_{c_1}
$$

**Implementación:**

```scala
def solapan(c1: Curso, c2: Curso): Boolean =
  iniCurso(c1) < finCurso(c2) && iniCurso(c2) < finCurso(c1)
```

**Argumento de corrección:**

La implementación traduce directamente la especificación matemática. Dos intervalos
semiabiertos $[\text{ini}_1, \text{fin}_1)$ y $[\text{ini}_2, \text{fin}_2)$ tienen
intersección no vacía si y solo si ninguno termina antes de que el otro comience:

$$
[\text{ini}_1, \text{fin}_1) \cap [\text{ini}_2, \text{fin}_2) \neq \emptyset
\iff \text{ini}_1 < \text{fin}_2 \;\land\; \text{ini}_2 < \text{fin}_1
$$

El operador `&&` con cortocircuito produce exactamente el mismo valor de verdad que
$\land$. Por tanto la función es correcta.

**Casos borde:**

- Intervalos adyacentes $[0,4)$ y $[4,8)$: $0 < 8 \land 4 < 4$ → `true ∧ false` → `false` ✓
- Contención total $[2,10)$ y $[4,6)$: $2 < 6 \land 4 < 10$ → `true` ✓
- Mismos límites $[4,8)$ y $[4,8)$: $4 < 8 \land 4 < 8$ → `true` ✓

---

### Corrección de `choques`

**Especificación:** el número de pares de cursos que comparten aula y se solapan:

$$
\text{CH}^\alpha_C = \bigl|\{(i,j) \mid 0 \le i < j < n,\; \alpha_i = \alpha_j,\; \alpha_i \ge 0,\; c_i \text{ solapa con } c_j\}\bigr|
$$

**Implementación:**

```scala
def choques(cursos: Cursos, a: Asignacion): Int = {
  val indices = cursos.indices.toVector
  indices.flatMap { i =>
    indices.filter { j =>
      j > i && a(i) == a(j) && a(i) >= 0 && solapan(cursos(i), cursos(j))
    }
  }.length
}
```

**Argumento de corrección:**

Por la regla de traducción de `flatMap` con `filter`:

$$
\texttt{indices.flatMap}(i \Rightarrow \texttt{indices.filter}(j \Rightarrow P(i,j)))
$$

produce exactamente la lista de $j$ que satisfacen $P(i,j)$ para cada $i$, concatenada
en orden. Las condiciones del `filter` corresponden exactamente a la especificación:

| Condición en código | Condición en especificación |
|:-------------------:|:---------------------------:|
| `j > i` | $i < j$ |
| `a(i) == a(j)` | $\alpha_i = \alpha_j$ |
| `a(i) >= 0` | $\alpha_i \ge 0$ |
| `solapan(cursos(i), cursos(j))` | $c_i$ solapa con $c_j$ |

Cada par $(i,j)$ con $i < j$ se genera exactamente una vez (cuando el índice externo
es $i$), por lo que no hay duplicados. `.length` cuenta los pares válidos.

**Conclusión:** $\forall\, C, \alpha : P_{\text{choques}}(C, \alpha) = \text{CH}^\alpha_C$ ✓

**Verificación cuantitativa con Ejemplo 1:**

$$
n = 3,\; m = 2 \implies \binom{3}{2} = 3 \text{ pares posibles}
$$

Solo el par $(0,1)$ cumple todas las condiciones → $\text{CH} = 1$. La implementación
produce 1. ✓

---

### Corrección de `capacidadFallida`

**Especificación:** número de cursos cuya aula asignada no tiene capacidad suficiente:

$$
\text{CF}^\alpha_{C,A} = \bigl|\{i \mid 0 \le i < n,\; \alpha_i \ge 0,\; \text{cap}_{A_{\alpha_i}} < \text{est}_{c_i}\}\bigr|
$$

**Implementación:**

```scala
def capacidadFallida(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = {
  cursos.indices.toVector.filter { i =>
    a(i) >= 0 && capAula(aulas(a(i))) < estCurso(cursos(i))
  }.length
}
```

**Argumento de corrección:**

`cursos.indices.toVector` genera $\{0,\ldots,n-1\}$, el dominio completo de índices.
El `filter` retiene exactamente los $i$ donde:

1. $\alpha_i \ge 0$ → el curso está asignado a algún aula.
2. $\text{cap}_{A_{\alpha_i}} < \text{est}_{c_i}$ → la capacidad es insuficiente.

Estas son exactamente las condiciones de la especificación. `.length` cuenta los índices retenidos.

**Casos borde:**
- $\text{cap} = \text{est}$: condición `<` es `false` → no falla. Correcto: capacidad exacta es válida. ✓
- Ningún curso asignado ($\alpha_i = -1$ para todo $i$): `filter` descarta todo → CF = 0. ✓

**Conclusión:** $\forall\, C, A, \alpha : P_{\text{CF}}(C, A, \alpha) = \text{CF}^\alpha_{C,A}$ ✓

---

### Corrección de `desperdicio`

**Especificación:** suma del exceso de capacidad sobre los cursos bien asignados:

$$
\text{DE}^\alpha_{C,A} = \sum_{\substack{i=0\\\alpha_i \ge 0}}^{n-1} \max\!\bigl(\text{cap}_{A_{\alpha_i}} - \text{est}_{c_i},\; 0\bigr)
$$

**Implementación:**

```scala
def desperdicio(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = {
  cursos.indices.toVector.filter { i =>
    a(i) >= 0 && capAula(aulas(a(i))) >= estCurso(cursos(i))
  }.map { i =>
    capAula(aulas(a(i))) - estCurso(cursos(i))
  }.sum
}
```

**Argumento de corrección:**

El pipeline tiene tres etapas:

**Etapa 1 — `filter`:** selecciona los $i$ con $\alpha_i \ge 0$ y $\text{cap} \ge \text{est}$.
Esto implementa el $\max(\cdot,0)$ de la especificación: si $\text{cap} < \text{est}$,
la diferencia sería negativa y se excluye con contribución 0.

**Etapa 2 — `map`:** transforma cada $i$ retenido en $\text{cap}_{A_{\alpha_i}} - \text{est}_{c_i} \ge 0$.

**Etapa 3 — `sum`:** acumula el total.

**Lema de equivalencia del** $\max$**:**

$$
\texttt{filter}(\text{cap} \ge \text{est}) \circ \texttt{map}(\text{cap} - \text{est})
\;\equiv\;
\texttt{map}\!\left(\max(\text{cap} - \text{est},\, 0)\right)
$$

*Demostración:* Si $\text{cap} \ge \text{est}$, ambas expresiones producen $\text{cap} - \text{est} \ge 0$.
Si $\text{cap} < \text{est}$, el `filter` excluye el elemento (contribución 0) y
$\max(\text{cap} - \text{est}, 0) = 0$. Son equivalentes en todos los casos.

**Conclusión:** $\forall\, C, A, \alpha : P_{\text{DE}}(C, A, \alpha) = \text{DE}^\alpha_{C,A}$ ✓

---

## Casos de prueba — Integrante 1

```scala
// --- solapan ---

test("solapan: M01[4,8) y M02[6,10) se solapan") {
  assert(solapan(("M01", 4, 8, 25), ("M02", 6, 10, 30)))
}
test("solapan: M01[4,8) y M03[12,16) no se solapan") {
  assert(!solapan(("M01", 4, 8, 25), ("M03", 12, 16, 20)))
}
test("solapan: cursos adyacentes [0,4) y [4,8) no se solapan") {
  assert(!solapan(("A", 0, 4, 10), ("B", 4, 8, 10)))
}
test("solapan: solapamiento parcial al final del primero") {
  assert(solapan(("A", 0, 6, 10), ("B", 4, 10, 10)))
}
test("solapan: un curso contenido completamente dentro de otro") {
  assert(solapan(("X", 2, 10, 15), ("Y", 4, 6, 10)))
}
test("solapan: mismo horario exacto se solapa") {
  assert(solapan(("A", 4, 8, 20), ("B", 4, 8, 20)))
}
test("solapan: cursos con brecha entre ellos no se solapan") {
  assert(!solapan(("A", 0, 4, 10), ("B", 6, 10, 10)))
}
test("solapan: segundo empieza justo cuando el primero termina") {
  assert(!solapan(("A", 6, 10, 25), ("B", 10, 14, 30)))
}

// --- choques ---

test("choques: asignacion [0,0,1] tiene 1 choque") {
  assert(choques(c1, Vector(0, 0, 1)) == 1)
}
test("choques: asignacion [0,1,0] no tiene choques") {
  assert(choques(c1, Vector(0, 1, 0)) == 0)
}
test("choques: tres cursos solapados en la misma aula producen 3 choques") {
  val cursos = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 5, 7, 20))
  assert(choques(cursos, Vector(0, 0, 0)) == 3)
}
test("choques: misma aula pero cursos adyacentes no generan choque") {
  val cursos = Vector(("M01", 0, 4, 25), ("M02", 4, 8, 30))
  assert(choques(cursos, Vector(0, 0)) == 0)
}
test("choques: solapamiento en aulas distintas no cuenta") {
  assert(choques(c1, Vector(0, 1, 1)) == 0)
}
test("choques: un solo curso no genera choques") {
  val cursos = Vector(("M01", 4, 8, 25))
  assert(choques(cursos, Vector(0)) == 0)
}
test("choques: cuatro cursos con dos pares en conflicto") {
  val cursos = Vector(("A", 0, 6, 10), ("B", 4, 8, 10), ("C", 2, 5, 10), ("D", 3, 7, 10))
  assert(choques(cursos, Vector(0, 0, 1, 1)) == 2)
}

// --- capacidadFallida ---

test("capacidadFallida: asignacion [0,0,1] no falla capacidad") {
  assert(capacidadFallida(c1, a1, Vector(0, 0, 1)) == 0)
}
test("capacidadFallida: asignacion [0,1,0] tampoco falla capacidad") {
  assert(capacidadFallida(c1, a1, Vector(0, 1, 0)) == 0)
}
test("capacidadFallida: una aula insuficiente") {
  val cursos = Vector(("X", 0, 4, 35), ("Y", 4, 8, 20))
  val aulas  = Vector(("E101", 30), ("E102", 40))
  assert(capacidadFallida(cursos, aulas, Vector(0, 0)) == 1)
}
test("capacidadFallida: todas las aulas insuficientes") {
  val cursos = Vector(("A", 0, 4, 25), ("B", 4, 8, 30))
  val aulas  = Vector(("E001", 10))
  assert(capacidadFallida(cursos, aulas, Vector(0, 0)) == 2)
}
test("capacidadFallida: capacidad exactamente igual no falla") {
  val cursos = Vector(("A", 0, 4, 30))
  val aulas  = Vector(("E101", 30))
  assert(capacidadFallida(cursos, aulas, Vector(0)) == 0)
}
test("capacidadFallida: ejemplo 2 asignacion [0,1,0,1] tiene 1 fallo") {
  val c2 = Vector(("F01",0,4,40),("F02",4,8,25),("F03",8,12,50),("F04",12,16,15))
  val a2 = Vector(("S201",45),("S202",30))
  assert(capacidadFallida(c2, a2, Vector(0, 1, 0, 1)) == 1)
}

// --- desperdicio ---

test("desperdicio: asignacion [0,0,1] tiene desperdicio 25") {
  assert(desperdicio(c1, a1, Vector(0, 0, 1)) == 25)
}
test("desperdicio: asignacion [0,1,0] tiene desperdicio 25") {
  assert(desperdicio(c1, a1, Vector(0, 1, 0)) == 25)
}
test("desperdicio: aula exactamente del tamaño del curso no desperdicia") {
  val cursos = Vector(("A", 0, 4, 30))
  val aulas  = Vector(("E101", 30))
  assert(desperdicio(cursos, aulas, Vector(0)) == 0)
}
test("desperdicio: aula insuficiente no suma al desperdicio") {
  val c2 = Vector(("F03", 8, 12, 50))
  val a2 = Vector(("S201", 45))
  assert(desperdicio(c2, a2, Vector(0)) == 0)
}
test("desperdicio: ejemplo 2 asignacion [0,1,0,1]") {
  val c2 = Vector(("F01",0,4,40),("F02",4,8,25),("F03",8,12,50),("F04",12,16,15))
  val a2 = Vector(("S201",45),("S202",30))
  assert(desperdicio(c2, a2, Vector(0, 1, 0, 1)) == 25)
}
test("desperdicio: un curso con mucho espacio sobrante") {
  val cursos = Vector(("A", 0, 4, 5))
  val aulas  = Vector(("E101", 50))
  assert(desperdicio(cursos, aulas, Vector(0)) == 45)
}
test("desperdicio: varios cursos todos con sobrante") {
  val cursos = Vector(("A", 0, 4, 10), ("B", 4, 8, 20), ("C", 8, 12, 30))
  val aulas  = Vector(("E101", 40))
  assert(desperdicio(cursos, aulas, Vector(0, 0, 0)) == 60)
}
### Corrección de `movilidad`
 
**Especificación:** ordenar los cursos asignados por hora de inicio y sumar las
distancias entre aulas consecutivas:
 
$$
\text{MV}^\alpha_{C,A,D} = \sum_{j=0}^{k-2} D\bigl[\alpha_{\sigma_j},\, \alpha_{\sigma_{j+1}}\bigr]
$$
 
donde $\sigma_0, \ldots, \sigma_{k-1}$ es la permutación que ordena los cursos
asignados por $\text{ini}_{c_{\sigma_j}}$ de forma creciente, y $k$ es el número
de cursos asignados ($\alpha_i \ge 0$).
 
**Implementación:**
 
```scala
def movilidad(cursos: Cursos, aulas: Aulas, d: Distancias,
              a: Asignacion): Int = {
  val asignados = cursos.indices.toVector
    .filter(i => a(i) >= 0)
    .sortBy(i => iniCurso(cursos(i)))
 
  def sumarDistancias(indices: Vector[Int]): Int =
    indices match {
      case _ if indices.length < 2 => 0
      case _ =>
        val dist = d(a(indices(0)))(a(indices(1)))
        dist + sumarDistancias(indices.tail)
    }
 
  sumarDistancias(asignados)
}
```

**Argumento de corrección:**

La implementación se divide en dos partes:

**Parte 1 — construcción de la secuencia ordenada:**

`filter(i => a(i) >= 0)` retiene exactamente los índices con $\alpha_i \ge 0$.
`sortBy(i => iniCurso(cursos(i)))` los ordena por hora de inicio, produciendo
la secuencia $\sigma_0, \ldots, \sigma_{k-1}$ de la especificación.

**Parte 2 — recursión sobre `sumarDistancias`:**

Se demuestra por inducción estructural sobre la longitud de `indices`:

- **Caso base** $|\texttt{indices}| < 2$: no hay par consecutivo, la suma es 0. Correcto según la especificación (suma vacía). ✓
- **Paso inductivo** $|\texttt{indices}| \ge 2$: la hipótesis de inducción establece que
  `sumarDistancias(indices.tail)` calcula correctamente $\sum_{j=1}^{k-2} D[\alpha_{\sigma_j}, \alpha_{\sigma_{j+1}}]$.
  La implementación agrega $D[\alpha_{\sigma_0}, \alpha_{\sigma_1}]$, produciendo la suma completa desde $j=0$. ✓
  **Casos borde:**
- $k = 0$ o $k = 1$: `length < 2` → retorna 0 (sin pares consecutivos). ✓
- Todos en la misma aula $j$: $D[j][j] = 0$ → $\text{MV} = 0$. ✓
  **Conclusión:** $\forall\, C, A, D, \alpha : \texttt{movilidad}(C, A, D, \alpha) = \text{MV}^\alpha_{C,A,D}$ ✓

**Verificación con Ejemplo 1:**

$\alpha = \langle 0, 0, 1 \rangle$, orden: M01(aula=0) → M02(aula=0) → M03(aula=1)

$$\text{MV} = D[0][0] + D[0][1] = 0 + 3 = 3 \checkmark$$
 
---

### Corrección de `costoAsignacion`

**Especificación:**

$$
\text{CT}^\alpha_{C,A,D} = w_{CH} \cdot \text{CH}^\alpha_C + w_{CF} \cdot \text{CF}^\alpha_{C,A} + w_{DE} \cdot \text{DE}^\alpha_{C,A} + w_{MV} \cdot \text{MV}^\alpha_{C,A,D}
$$

**Implementación:**

```scala
def costoAsignacion(cursos: Cursos, aulas: Aulas, d: Distancias,
                    a: Asignacion, w: Pesos): Int = {
  val (wCH, wCF, wDE, wMV) = w
  wCH * choques(cursos, a) +
  wCF * capacidadFallida(cursos, aulas, a) +
  wDE * desperdicio(cursos, aulas, a) +
  wMV * movilidad(cursos, aulas, d, a)
}
```

**Argumento de corrección:**

La implementación es una traducción directa de la fórmula de la especificación.
Dado que la corrección de `choques`, `capacidadFallida`, `desperdicio` y `movilidad`
ha sido demostrada individualmente, por sustitución:

$$
\texttt{costoAsignacion}(C, A, D, \alpha, w)
= w_{CH} \cdot \text{CH}^\alpha_C + w_{CF} \cdot \text{CF}^\alpha_{C,A}
+ w_{DE} \cdot \text{DE}^\alpha_{C,A} + w_{MV} \cdot \text{MV}^\alpha_{C,A,D}
  = \text{CT}^\alpha_{C,A,D}
  $$
  La corrección se reduce a la corrección de las funciones componentes y a la
  aritmética entera de Scala, que es correcta para los rangos del problema.

**Verificación con Ejemplo 1:**

$\alpha = \langle 0, 0, 1 \rangle$, $w = (1000, 100, 1, 2)$:

$$\text{CT} = 1000 \cdot 1 + 100 \cdot 0 + 1 \cdot 25 + 2 \cdot 3 = \mathbf{1031} \checkmark$$

$\alpha = \langle 0, 1, 0 \rangle$:

$$\text{CT} = 1000 \cdot 0 + 100 \cdot 0 + 1 \cdot 25 + 2 \cdot 6 = \mathbf{37} \checkmark$$

**Conclusión:** $\forall\, C, A, D, \alpha, w : \texttt{costoAsignacion}(C, A, D, \alpha, w) = \text{CT}^\alpha_{C,A,D}$ ✓
 
---

### Corrección de `generarAsignaciones`

**Especificación:** generar todos los vectores en $\{0,\ldots,m-1\}^n$:

$$
\texttt{generarAsignaciones}(n, m) = \{0,\ldots,m-1\}^n
$$

El tamaño del resultado debe ser $m^n$.

**Implementación:**

```scala
def generarAsignaciones(n: Int, m: Int): Vector[Asignacion] = {
  if (n == 0) Vector(Vector.empty[Int])
  else {
    val subAsignaciones = generarAsignaciones(n - 1, m)
    (0 until m).toVector.flatMap { aulaIdx =>
      subAsignaciones.map(sub => aulaIdx +: sub)
    }
  }
}
```

**Argumento de corrección por inducción sobre $n$:**

**Caso base** $n = 0$:

$$\{0,\ldots,m-1\}^0 = \{\langle\rangle\}$$

La implementación retorna `Vector(Vector.empty)`, que contiene exactamente el vector vacío. ✓

**Hipótesis de inducción:** `generarAsignaciones(n-1, m)` devuelve exactamente $\{0,\ldots,m-1\}^{n-1}$.

**Paso inductivo** $n > 0$:

Todo vector $\langle v_0, v_1, \ldots, v_{n-1}\rangle \in \{0,\ldots,m-1\}^n$ tiene
$v_0 \in \{0,\ldots,m-1\}$ y $\langle v_1,\ldots,v_{n-1}\rangle \in \{0,\ldots,m-1\}^{n-1}$.

La implementación construye exactamente estos vectores:
para cada `aulaIdx` $= v_0 \in \{0,\ldots,m-1\}$, antepone `aulaIdx` a cada
sub-asignación $s \in$ `subAsignaciones` $= \{0,\ldots,m-1\}^{n-1}$ (por HI).
El `flatMap` concatena todos los grupos, produciendo $\{0,\ldots,m-1\}^n$. ✓

**Tamaño:**

$$|\texttt{generarAsignaciones}(n, m)| = m \cdot |\texttt{generarAsignaciones}(n-1, m)| = m \cdot m^{n-1} = m^n \checkmark$$

**Conclusión:** $\forall\, n \ge 0, m \ge 1 : \texttt{generarAsignaciones}(n, m) = \{0,\ldots,m-1\}^n$ ✓
 
---

### Corrección de `asignacionOptima`

**Especificación:** hallar la asignación $\alpha^* \in \{0,\ldots,m-1\}^n$ que minimiza
el costo total:

$$
\alpha^* = \arg\min_{\alpha \in \{0,\ldots,m-1\}^n} \text{CT}^\alpha_{C,A,D}
$$

**Implementación:**

```scala
def asignacionOptima(cursos: Cursos, aulas: Aulas, d: Distancias,
                     w: Pesos): (Asignacion, Int) = {
  val candidatas = generarAsignaciones(cursos.length, aulas.length)
 
  def buscarMinimo(cs: Vector[Asignacion], mejorAsig: Asignacion,
                   mejorCosto: Int): (Asignacion, Int) =
    cs match {
      case Vector() => (mejorAsig, mejorCosto)
      case _ =>
        val costo = costoAsignacion(cursos, aulas, d, cs.head, w)
        if (costo < mejorCosto) buscarMinimo(cs.tail, cs.head, costo)
        else                    buscarMinimo(cs.tail, mejorAsig, mejorCosto)
    }
 
  val primera = candidatas.head
  buscarMinimo(candidatas.tail, primera,
               costoAsignacion(cursos, aulas, d, primera, w))
}
```

**Argumento de corrección:**

La corrección se establece a través del invariante del acumulador de `buscarMinimo`.

**Invariante:** al llamar `buscarMinimo(cs, mejorAsig, mejorCosto)`, se cumple:

$$
\texttt{mejorCosto} = \min_{\alpha \in \texttt{evaluadas}} \text{CT}^\alpha_{C,A,D}
\quad \land \quad
\texttt{costoAsignacion}(\texttt{mejorAsig}) = \texttt{mejorCosto}
$$

donde `evaluadas` es el conjunto de candidatas ya procesadas.

**Demostración por inducción estructural:**

- **Caso base** `cs = Vector()`: se han evaluado todas las candidatas. El acumulador contiene el mínimo global. Se devuelve `(mejorAsig, mejorCosto)`. ✓
- **Paso inductivo** `cs = head :: tail`: se calcula `costo = CT(head)`.
    - Si `costo < mejorCosto`: `head` es la nueva mejor. Se llama recursivamente con `(tail, head, costo)`. El invariante se mantiene. ✓
    - Si `costo >= mejorCosto`: el acumulador no cambia. Se llama con `(tail, mejorAsig, mejorCosto)`. El invariante se mantiene. ✓
      **Cobertura:** dado que `generarAsignaciones` produce exactamente $\{0,\ldots,m-1\}^n$
      (demostrado arriba), `buscarMinimo` evalúa todas las asignaciones posibles, garantizando
      que el mínimo encontrado es el global.

**Conclusión:** $\forall\, C, A, D, w : \texttt{asignacionOptima}(C, A, D, w)$ devuelve $(\alpha^*, \text{CT}^{\alpha^*})$ ✓
 
---

## Casos de prueba — Integrante 2

```scala
// --- movilidad ---
 
test("movilidad: asignacion [0,0,1] movilidad 3 segun enunciado") {
  assert(movilidad(c1, a1, d1, Vector(0, 0, 1)) == 3)
}
test("movilidad: asignacion [0,1,0] movilidad 6") {
  assert(movilidad(c1, a1, d1, Vector(0, 1, 0)) == 6)
}
test("movilidad: un solo curso asignado movilidad 0") {
  assert(movilidad(c1, a1, d1, Vector(0, -1, -1)) == 0)
}
test("movilidad: todos en la misma aula movilidad 0") {
  assert(movilidad(c1, a1, d1, Vector(0, 0, 0)) == 0)
}
test("movilidad: ejemplo 2 asignacion [0,1,0,1] movilidad 15") {
  assert(movilidad(c2, a2, d2, Vector(0, 1, 0, 1)) == 15)
}
test("movilidad: orden por hora de inicio respetado") {
  val cursos = Vector(("C", 2, 4, 10), ("A", 0, 2, 10), ("B", 6, 8, 10))
  assert(movilidad(cursos, a1, d1, Vector(1, 0, 0)) == 6)
}
 
// --- costoAsignacion ---
 
test("costoAsignacion: asignacion [0,0,1] cuesta 1031") {
  assert(costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w) == 1031)
}
test("costoAsignacion: asignacion [0,1,0] cuesta 37") {
  assert(costoAsignacion(c1, a1, d1, Vector(0, 1, 0), w) == 37)
}
test("costoAsignacion: ejemplo 2 asignacion [0,1,0,1] cuesta 155") {
  assert(costoAsignacion(c2, a2, d2, Vector(0, 1, 0, 1), w) == 155)
}
test("costoAsignacion: ejemplo 2 asignacion [0,1,1,0] cuesta 160") {
  assert(costoAsignacion(c2, a2, d2, Vector(0, 1, 1, 0), w) == 160)
}
test("costoAsignacion: un curso sin choques ni fallos solo desperdicio") {
  val cursos = Vector(("A", 0, 4, 10))
  val aulas  = Vector(("E101", 30))
  val dist   = Vector(Vector(0))
  assert(costoAsignacion(cursos, aulas, dist, Vector(0), w) == 20)
}
 
// --- generarAsignaciones ---
 
test("generarAsignaciones: 2 cursos 2 aulas produce 4 asignaciones") {
  assert(generarAsignaciones(2, 2).length == 4)
}
test("generarAsignaciones: 3 cursos 3 aulas produce 27 asignaciones") {
  assert(generarAsignaciones(3, 3).length == 27)
}
test("generarAsignaciones: 0 cursos produce una asignacion vacia") {
  val r = generarAsignaciones(0, 3)
  assert(r.length == 1 && r.head.isEmpty)
}
test("generarAsignaciones: 1 curso 3 aulas produce Vector(0), Vector(1), Vector(2)") {
  val r = generarAsignaciones(1, 3)
  assert(r.contains(Vector(0)) && r.contains(Vector(1)) && r.contains(Vector(2)))
}
test("generarAsignaciones: cada asignacion tiene exactamente n elementos") {
  assert(generarAsignaciones(4, 3).forall(_.length == 4))
}
 
// --- asignacionOptima ---
 
test("asignacionOptima: costo optimo no supera 37 en ejemplo 1") {
  val (_, costo) = asignacionOptima(c1, a1, d1, w)
  assert(costo <= 37)
}
test("asignacionOptima: asignacion devuelta tiene n elementos") {
  val (asig, _) = asignacionOptima(c1, a1, d1, w)
  assert(asig.length == c1.length)
}
test("asignacionOptima: indices de aula validos") {
  val (asig, _) = asignacionOptima(c1, a1, d1, w)
  assert(asig.forall(j => j >= 0 && j < a1.length))
}
test("asignacionOptima: costo reportado coincide con costoAsignacion") {
  val (asig, costo) = asignacionOptima(c1, a1, d1, w)
  assert(costoAsignacion(c1, a1, d1, asig, w) == costo)
}
test("asignacionOptima: un curso una aula devuelve [0]") {
  val cursos = Vector(("A", 0, 4, 20))
  val aulas  = Vector(("E101", 30))
  val dist   = Vector(Vector(0))
  val (asig, _) = asignacionOptima(cursos, aulas, dist, w)
  assert(asig == Vector(0))
}
```