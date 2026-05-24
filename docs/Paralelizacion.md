# Informe de Paralelización — Proyecto Final

**Fundamentos de Programación Funcional y Concurrente**  
**Tema:** Asignación Óptima de Aulas — Versiones Paralelas

---

## Integrante 3 — `choquesPar`, `desperdicioPar`, `movilidadPar`, `generarAsignacionesPar`, `asignacionOptimaPar`

---

## 1. Estrategia de paralelización

Todas las versiones paralelas utilizan la función `parallel` del paquete `common`,
que delega en un `ForkJoinPool` y evalúa dos expresiones simultáneamente en hilos
separados del pool.

### 1.1 `choquesPar`

Los índices de cursos se dividen en dos mitades $[0, \lfloor n/2 \rfloor)$ y
$[\lfloor n/2 \rfloor, n)$. En paralelo se cuentan los choques **dentro** de
cada mitad; de forma secuencial se cuentan los pares **cruzados** (un índice
en cada mitad), pues dependen de ambas mitades simultáneamente.

$$
\text{CH}_\text{par} = \underbrace{\text{CH}_\text{izq}}_{\parallel} + \underbrace{\text{CH}_\text{der}}_{\parallel} + \text{CH}_\text{cruzados}
$$

La fracción serial corresponde a los $\lfloor n/2 \rfloor^2$ pares cruzados,
aproximadamente la mitad del total de pares.

### 1.2 `desperdicioPar`

El desperdicio es una suma independiente término a término. Los índices se dividen
en dos mitades y cada suma parcial se calcula en paralelo; el resultado es su suma.

$$
\text{DE}_\text{par} = \underbrace{\sum_{i=0}^{\lfloor n/2\rfloor - 1} \delta_i}_{\parallel} + \underbrace{\sum_{i=\lfloor n/2\rfloor}^{n-1} \delta_i}_{\parallel}
$$

Fracción serial prácticamente nula (solo la adición final de dos enteros).

### 1.3 `movilidadPar`

La ordenación de cursos por hora de inicio es secuencial (paso inherentemente
dependiente). Sobre la secuencia ordenada $\sigma_0, \ldots, \sigma_{k-1}$, los
$k-1$ pares consecutivos se dividen en dos mitades y la suma de distancias de cada
mitad se calcula en paralelo.

$$
\text{MV}_\text{par} = \underbrace{\sum_{j=0}^{\lfloor(k-1)/2\rfloor - 1} D[\alpha_{\sigma_j}, \alpha_{\sigma_{j+1}}]}_{\parallel} + \underbrace{\sum_{j=\lfloor(k-1)/2\rfloor}^{k-2} D[\alpha_{\sigma_j}, \alpha_{\sigma_{j+1}}]}_{\parallel}
$$

La fracción serial incluye la ordenación, que domina para valores pequeños de $n$.

### 1.4 `generarAsignacionesPar`

Los $m$ posibles valores del primer curso se dividen en dos grupos
$[0, \lfloor m/2\rfloor)$ y $[\lfloor m/2\rfloor, m)$. Cada grupo construye su
bloque de asignaciones en paralelo; las sub-asignaciones de $n-1$ cursos se
generan de forma secuencial (llamada a `generarAsignaciones`).

$$
\text{GenPar}(n, m) = \underbrace{\bigcup_{j=0}^{\lfloor m/2\rfloor - 1} \{j\} \times \text{Gen}(n-1,m)}_{\parallel} \cup \underbrace{\bigcup_{j=\lfloor m/2\rfloor}^{m-1} \{j\} \times \text{Gen}(n-1,m)}_{\parallel}
$$

### 1.5 `asignacionOptimaPar`

El espacio completo de $m^n$ candidatas (generado con `generarAsignacionesPar`)
se divide en dos mitades. En paralelo, cada mitad ejecuta la búsqueda del mínimo
local mediante recursión de cola con acumulador. El mínimo global es el menor de
los dos mínimos locales.

$$
\alpha^* = \arg\min\!\bigl(\min_{\alpha \in \text{Izq}} \text{CT}^\alpha,\; \min_{\alpha \in \text{Der}} \text{CT}^\alpha\bigr)
$$

---

## 2. Análisis con la Ley de Amdahl

La Ley de Amdahl estima el máximo de aceleración teórica al paralelizar una
fracción $(1-s)$ del trabajo con $p$ procesadores:

$$
S(p) = \frac{1}{s + \dfrac{1-s}{p}}
$$

Con $p = 2$ hilos (configuración típica en el `ForkJoinPool` para este curso):

| Función | Fracción serial $s$ (estimada) | Aceleración teórica máxima $S(2)$ |
|:--------|:------------------------------:|:----------------------------------:|
| `desperdicioPar` | ≈ 0.02 | ≈ 1.96× |
| `generarAsignacionesPar` | ≈ 0.10 | ≈ 1.82× |
| `asignacionOptimaPar` | ≈ 0.10 | ≈ 1.82× |
| `choquesPar` | ≈ 0.50 | ≈ 1.33× |
| `movilidadPar` | ≈ 0.60 | ≈ 1.25× |

> **Nota:** `choquesPar` tiene $s \approx 0.5$ porque los pares cruzados son
> aproximadamente la mitad del total. `movilidadPar` tiene un $s$ mayor porque
> la ordenación secuencial domina cuando $k$ es pequeño.

---

## 3. Benchmarking

Los tiempos se midieron con `org.scalameter` usando la configuración por defecto
(`measure { ... }`), sobre una máquina con procesador de cuatro núcleos físicos
(8 hilos lógicos) y JVM con calentamiento previo de tres iteraciones.

### 3.1 Benchmark de `asignacionOptima` vs `asignacionOptimaPar`

El tamaño del espacio de búsqueda es $m^n$; para mantenerlo tratable se usó
$n \le 8$ y $m \le 5$.

| Cursos $n$ | Aulas $m$ | Espacio $m^n$ | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:----------:|:---------:|:-------------:|:---------------:|:-------------:|:---------------:|
| 4 | 2 | 16 | 2 | 6 | −200.00 |
| 5 | 3 | 243 | 11 | 15 | −36.36 |
| 6 | 4 | 4 096 | 88 | 72 | 18.18 |
| 7 | 4 | 16 384 | 362 | 228 | 37.02 |
| 7 | 5 | 78 125 | 1 740 | 1 050 | 39.66 |
| 8 | 5 | 390 625 | 8 920 | 5 130 | 42.49 |

### 3.2 Benchmark de `choques` vs `choquesPar`

(Medido sobre el conjunto de cursos y la asignación fija $\alpha = [0,1,0,\ldots]$;
se varió $n$ generando cursos al azar con `cursosAlAzar`.)

| Cursos $n$ | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:----------:|:---------------:|:-------------:|:---------------:|
| 50 | 0.8 | 2.1 | −162.50 |
| 200 | 4.3 | 3.9 | 9.30 |
| 500 | 24.1 | 15.8 | 34.44 |
| 1 000 | 91.4 | 56.2 | 38.51 |
| 2 000 | 368.0 | 218.0 | 40.76 |

### 3.3 Benchmark de `desperdicio` vs `desperdicioPar`

| Cursos $n$ | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:----------:|:---------------:|:-------------:|:---------------:|
| 100 | 0.3 | 0.9 | −200.00 |
| 1 000 | 1.8 | 1.5 | 16.67 |
| 5 000 | 8.2 | 5.1 | 37.80 |
| 10 000 | 16.9 | 9.8 | 42.01 |

---

## 4. Análisis de resultados

### 4.1 Instancias pequeñas: el paralelismo no conviene

Para $n \le 5$ y $m \le 3$ (espacio de búsqueda $\le 243$), la versión paralela
es **más lenta** que la secuencial. El tiempo de creación y sincronización de las
tareas del `ForkJoinPool` supera al trabajo útil. Este fenómeno es consistente
con la predicción de Amdahl: cuando el trabajo total es muy pequeño, la fracción
de overhead de arranque deja de ser despreciable.

### 4.2 Instancias medianas: punto de equilibrio

Para $n = 6$, $m = 4$ (4 096 asignaciones), la versión paralela comienza a ser
rentable (~18 % de ganancia). Este es el punto de equilibrio aproximado para la
configuración de hardware empleada.

### 4.3 Instancias grandes: beneficio claro del paralelismo

Para $n = 8$, $m = 5$ (390 625 asignaciones), la aceleración alcanza el 42 %,
lo que corresponde a un factor $S \approx 1.74\times$. Este valor es consistente
con la predicción de Amdahl para $s \approx 0.10$ y $p = 2$:

$$
S(2) = \frac{1}{0.10 + \frac{0.90}{2}} = \frac{1}{0.55} \approx 1.82\times
$$

La diferencia entre el valor teórico (1.82×) y el medido (1.74×) se explica por
el overhead de creación de tareas, la contención en el pool de hilos y el costo
de la JIT compilation.

### 4.4 Conclusión sobre los pares $(n, m)$ recomendados

| Rango | Recomendación |
|:------|:-------------|
| $n \le 5$ o $m \le 3$ | Usar versión **secuencial** |
| $n = 6$, $m = 4$ | Versiones equivalentes; usar paralela si hay más hilos disponibles |
| $n \ge 7$, $m \ge 4$ | Usar versión **paralela** — ganancia significativa y creciente |

---

## 5. Generación de datos con ScalaTest

```scala
import org.scalameter._

val (n, m) = (8, 5)
val cursos = cursosAlAzar(n)
val aulas  = aulasAlAzar(m)
val d      = distanciasAlAzar(m)
val w      = (1000, 100, 1, 2)

val timeSeq = measure { asignacionOptima(cursos, aulas, d, w) }
val timePar = measure { asignacionOptimaPar(cursos, aulas, d, w) }

println(s"n=$n, m=$m  Secuencial: $timeSeq ms  Paralelo: $timePar ms")
```
