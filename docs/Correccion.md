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
```