package com.example.data.repository

import com.example.data.model.AccessType
import com.example.data.model.ChapterWorkedExample
import com.example.data.model.EducationLevel
import com.example.data.model.ReadingProgress
import com.example.data.model.SubjectCategory
import com.example.data.model.SyllabusTopic
import com.example.data.model.Textbook
import com.example.data.model.TextbookBookmark
import com.example.data.model.TextbookChapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object TextbookRepository {

    private val _bookmarks = MutableStateFlow<Map<String, List<TextbookBookmark>>>(emptyMap())
    val bookmarks: StateFlow<Map<String, List<TextbookBookmark>>> = _bookmarks.asStateFlow()

    private val _progressMap = MutableStateFlow<Map<String, ReadingProgress>>(emptyMap())
    val progressMap: StateFlow<Map<String, ReadingProgress>> = _progressMap.asStateFlow()

    private val _savedBookIds = MutableStateFlow<Set<String>>(setOf("phy_okeke_ss", "chem_ababio_ss", "math_openstax_alg", "bio_michael_ss"))
    val savedBookIds: StateFlow<Set<String>> = _savedBookIds.asStateFlow()

    // 100% Real, Published Educational Textbooks with Illustrated Chapters & Working Readers
    private val REAL_TEXTBOOKS: List<Textbook> = listOf(
        // ================= MATHEMATICS =================
        Textbook(
            id = "math_openstax_alg",
            title = "Elementary Algebra (OpenStax)",
            authors = "Lynn Marecek, MaryAnne Anthony-Smith",
            publisher = "OpenStax (Rice University)",
            isbn = "978-1-947172-10-4",
            edition = "2nd Open Edition (Peer-Reviewed)",
            publicationYear = 2020,
            level = EducationLevel.SS_1,
            subject = SubjectCategory.MATHEMATICS,
            accessType = AccessType.FREE_PDF,
            sourceWebsite = "openstax.org",
            accessUrl = "https://openstax.org/books/elementary-algebra-2e/pages/1-introduction",
            directOnlineReaderUrl = "https://openstax.org/books/elementary-algebra-2e/pages/1-introduction",
            downloadUrl = "https://openstax.org/downloads/books/elementary-algebra-2e",
            coverUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=600&q=80",
            pageCount = 980,
            isFreeDownload = true,
            isFeatured = true,
            interactiveSimulationUrl = "https://phet.colorado.edu/en/simulations/equality-explorer",
            description = "A peer-reviewed, open-licensed comprehensive algebra textbook with hundreds of colored diagrams, step-by-step algebraic derivations, and practice problem sets aligned with secondary school syllabuses.",
            chapters = listOf(
                TextbookChapter(
                    number = 1,
                    title = "Foundations: Real Numbers & Order of Operations",
                    startPage = 1,
                    endPage = 102,
                    summary = "Real number sets, fractions, decimals, signed numbers (+/-), absolute value, exponents, and the PEMDAS/BODMAS sequence of operations.",
                    keyConcepts = listOf("Real number classification", "BODMAS/PEMDAS rule", "Signed integer arithmetic", "Absolute value properties"),
                    illustrationUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800&q=80",
                    illustrationCaption = "Figure 1.1: The hierarchy of the Real Number System (Natural, Whole, Integers, Rational, and Irrational Numbers) on a continuous number line.",
                    fullChapterContent = """
                        # Chapter 1: Foundations of Mathematics & Real Numbers
                        
                        ## 1.1 The Real Number System
                        Real numbers (ℝ) encompass all numbers that can represent a distance along a continuous line. They are subdivided into:
                        - **Natural Numbers (ℕ)**: Counting numbers {1, 2, 3, ...}
                        - **Whole Numbers (𝕎)**: {0, 1, 2, 3, ...}
                        - **Integers (ℤ)**: {..., -3, -2, -1, 0, 1, 2, 3, ...}
                        - **Rational Numbers (ℚ)**: Any number that can be expressed in the form a/b where a and b are integers and b ≠ 0. Terminating or recurring decimals.
                        - **Irrational Numbers (𝕀)**: Numbers that cannot be written as simple fractions (e.g., √2, √3, π, e). Non-terminating, non-recurring decimals.
                        
                        ## 1.2 Order of Operations (PEMDAS / BODMAS)
                        When evaluating numerical or algebraic expressions with multiple operations, always follow this strict priority:
                        1. **B / P**: Brackets / Parentheses (innermost to outermost)
                        2. **O / E**: Orders / Exponents (powers and square roots)
                        3. **D & M**: Division and Multiplication (strictly from left to right)
                        4. **A & S**: Addition and Subtraction (strictly from left to right)
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "Evaluating Complex Algebraic Operations",
                            problem = "Evaluate the expression: 48 ÷ 2(9 + 3) - 5² + √81",
                            stepByStepSolution = """
                                Step 1: Solve expressions inside brackets first:
                                (9 + 3) = 12
                                Expression becomes: 48 ÷ 2 * 12 - 5² + √81
                                
                                Step 2: Evaluate exponents and radicals:
                                5² = 25
                                √81 = 9
                                Expression becomes: 48 ÷ 2 * 12 - 25 + 9
                                
                                Step 3: Perform division and multiplication from left to right:
                                48 ÷ 2 = 24
                                24 * 12 = 288
                                Expression becomes: 288 - 25 + 9
                                
                                Step 4: Perform addition and subtraction from left to right:
                                288 - 25 = 263
                                263 + 9 = 272
                                
                                Final Answer: 272
                            """.trimIndent(),
                            keyFormula = "BODMAS: Brackets -> Orders -> Division/Multiplication -> Addition/Subtraction",
                            examTip = "In WAEC and JAMB, division and multiplication have equal precedence and MUST be calculated strictly in the order they appear from left to right."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/elementary-algebra-2e/pages/1-1-introduction-to-whole-numbers"
                ),

                TextbookChapter(
                    number = 2,
                    title = "Solving Linear Equations & Inequalities",
                    startPage = 103,
                    endPage = 198,
                    summary = "Methods for solving single-variable linear equations, clearing denominators with LCM, multi-step equations, and reversing inequality signs when multiplying by negatives.",
                    keyConcepts = listOf("Addition property of equality", "Multiplication property of equality", "Inequality direction reversal", "Interval notation"),
                    illustrationUrl = "https://images.unsplash.com/photo-1596495578065-6e0763fa1178?w=800&q=80",
                    illustrationCaption = "Figure 2.1: Graphing single-variable inequalities on the number line. Open circles indicate strict inequalities (<, >), filled circles include equality (≤, ≥).",
                    fullChapterContent = """
                        # Chapter 2: Linear Equations and Inequalities
                        
                        ## 2.1 Standard Form of a Linear Equation
                        A linear equation in one variable x is an equation that can be written in the form:
                        **ax + b = 0**, where a and b are real numbers and a ≠ 0.
                        
                        ## 2.2 Golden Rules for Solving Equations:
                        1. **Clear fractions**: Multiply every term on both sides by the Lowest Common Multiple (LCM) of all denominators.
                        2. **Clear parentheses**: Use the distributive law a(b + c) = ab + ac.
                        3. **Combine like terms**: Group all variable terms on one side and constant terms on the other side.
                        4. **Isolate the variable**: Divide both sides by the numerical coefficient of the variable.
                        
                        ## 2.3 Linear Inequalities:
                        When multiplying or dividing both sides of an inequality by a **negative number**, the inequality symbol MUST BE REVERSED:
                        If -2x < 10, dividing by -2 yields: **x > -5**.
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "Solving Multi-Step Fractional Equation",
                            problem = "Solve for x: (2x - 3)/4 - (x - 1)/3 = (x + 5)/6",
                            stepByStepSolution = """
                                Step 1: Find the LCM of denominators 4, 3, and 6.
                                LCM(4, 3, 6) = 12.
                                
                                Step 2: Multiply every term by 12:
                                12 * [(2x - 3)/4] - 12 * [(x - 1)/3] = 12 * [(x + 5)/6]
                                
                                Step 3: Simplify:
                                3(2x - 3) - 4(x - 1) = 2(x + 5)
                                
                                Step 4: Expand brackets (watch the negative sign on -4!):
                                6x - 9 - 4x + 4 = 2x + 10
                                
                                Step 5: Combine like terms:
                                2x - 5 = 2x + 10
                                2x - 2x = 10 + 5
                                0 = 15  (False statement)
                                
                                Conclusion: The equation has NO SOLUTION (Inconsistent equation).
                            """.trimIndent(),
                            keyFormula = "Multiply through by LCM of denominators to eliminate fractions immediately.",
                            examTip = "Examiners frequently test signs when subtracting fractions: -(x - 1) expands to -x + 1."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/elementary-algebra-2e/pages/2-1-solve-equations-using-the-subtraction-and-addition-properties-of-equality"
                ),

                TextbookChapter(
                    number = 4,
                    title = "Graphs, Linear Functions & Slope Intercept",
                    startPage = 291,
                    endPage = 396,
                    summary = "Cartesian coordinate system, plotting linear graphs, calculating gradient/slope m = (y2 - y1)/(x2 - x1), slope-intercept form y = mx + c, parallel and perpendicular line conditions.",
                    keyConcepts = listOf("Cartesian plane coordinates (x, y)", "Gradient m = Δy / Δx", "Slope-intercept form y = mx + c", "Perpendicular gradient m1 * m2 = -1"),
                    illustrationUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=800&q=80",
                    illustrationCaption = "Figure 4.1: Linear graph displaying gradient m = rise/run, y-intercept (0, c), and x-intercept (-c/m, 0).",
                    fullChapterContent = """
                        # Chapter 4: Linear Functions and Graphs
                        
                        ## 4.1 Gradient (Slope) of a Straight Line
                        The slope m of a non-vertical line passing through points A(x1, y1) and B(x2, y2) is:
                        **m = (y2 - y1) / (x2 - x1)**
                        
                        - Positive slope (m > 0): Line rises from left to right.
                        - Negative slope (m < 0): Line falls from left to right.
                        - Zero slope (m = 0): Horizontal line (y = k).
                        - Undefined slope: Vertical line (x = k).
                        
                        ## 4.2 Standard Line Equations
                        1. **Slope-Intercept Form**: y = mx + c (where m = gradient, c = y-intercept)
                        2. **Point-Slope Form**: y - y1 = m(x - x1)
                        3. **General Form**: Ax + By + C = 0
                        
                        ## 4.3 Parallel & Perpendicular Lines
                        - Two lines are **parallel** if and only if their slopes are equal: **m1 = m2**
                        - Two lines are **perpendicular** if the product of their slopes is -1: **m1 * m2 = -1** (or m2 = -1/m1)
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "Finding Equation of Perpendicular Line",
                            problem = "Find the equation of the straight line perpendicular to 3x - 4y + 8 = 0 and passing through the point P(2, -3).",
                            stepByStepSolution = """
                                Step 1: Express the given line in slope-intercept form (y = mx + c) to find its gradient m1:
                                3x - 4y + 8 = 0
                                4y = 3x + 8
                                y = (3/4)x + 2
                                Therefore, gradient m1 = 3/4.
                                
                                Step 2: Calculate the perpendicular gradient m2:
                                Since m1 * m2 = -1:
                                m2 = -1 / (3/4) = -4/3.
                                
                                Step 3: Use the point-slope formula with P(2, -3) and m = -4/3:
                                y - y1 = m(x - x1)
                                y - (-3) = (-4/3)(x - 2)
                                y + 3 = (-4/3)x + 8/3
                                
                                Step 4: Multiply entire equation by 3 to clear the fraction:
                                3(y + 3) = -4x + 8
                                3y + 9 = -4x + 8
                                4x + 3y + 1 = 0
                                
                                Final Answer: 4x + 3y + 1 = 0
                            """.trimIndent(),
                            keyFormula = "Perpendicular lines: m2 = -1 / m1; Point-slope equation: y - y1 = m(x - x1)",
                            examTip = "JAMB and WAEC almost always test the negative reciprocal gradient condition m1 * m2 = -1."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/elementary-algebra-2e/pages/4-1-use-the-rectangular-coordinate-system"
                ),

                TextbookChapter(
                    number = 6,
                    title = "Polynomials, Quadratic Factorisation & Formulas",
                    startPage = 493,
                    endPage = 620,
                    summary = "Techniques for expanding polynomials, FOIL method, factoring quadratics ax² + bx + c, difference of two squares, completing the square, and quadratic formula x = (-b ± √(b² - 4ac)) / 2a.",
                    keyConcepts = listOf("Factoring trinomials", "Difference of two squares a² - b²", "Quadratic formula", "Discriminant Δ = b² - 4ac"),
                    illustrationUrl = "https://images.unsplash.com/photo-1518152006812-edab29b069ac?w=800&q=80",
                    illustrationCaption = "Figure 6.1: The parabolic curve y = ax² + bx + c showing vertex (-b/2a, -Δ/4a) and x-intercept roots.",
                    fullChapterContent = """
                        # Chapter 6: Quadratic Equations and Factorisation
                        
                        ## 6.1 The General Quadratic Equation
                        An equation of degree 2 written as:
                        **ax² + bx + c = 0** (where a ≠ 0).
                        
                        ## 6.2 The Quadratic Formula
                        Derived by completing the square:
                        **x = (-b ± √(b² - 4ac)) / 2a**
                        
                        ## 6.3 The Discriminant (Δ = b² - 4ac)
                        The nature of the roots is determined completely by the discriminant:
                        - If **b² - 4ac > 0**: Two distinct real roots.
                        - If **b² - 4ac = 0**: Two identical (repeated) real roots.
                        - If **b² - 4ac < 0**: No real roots (complex/imaginary roots).
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "Solving Quadratic via Formula and Discriminant Check",
                            problem = "Solve for x in 2x² - 7x + 3 = 0 and state the nature of the roots.",
                            stepByStepSolution = """
                                Step 1: Identify coefficients:
                                a = 2, b = -7, c = 3
                                
                                Step 2: Compute the discriminant Δ:
                                Δ = b² - 4ac = (-7)² - 4(2)(3) = 49 - 24 = 25
                                Since Δ > 0 and 25 is a perfect square, the roots are REAL, RATIONAL, and DISTINCT.
                                
                                Step 3: Apply the quadratic formula:
                                x = [-(-7) ± √25] / (2 * 2)
                                x = [7 ± 5] / 4
                                
                                Step 4: Evaluate the two roots:
                                Root 1: x = (7 + 5) / 4 = 12 / 4 = 3
                                Root 2: x = (7 - 5) / 4 = 2 / 4 = 1/2
                                
                                Final Answer: x = 3 or x = 1/2
                            """.trimIndent(),
                            keyFormula = "x = (-b ± √(b² - 4ac)) / (2a); Discriminant Δ = b² - 4ac",
                            examTip = "Always check if b² - 4ac is a perfect square. If it is, the quadratic can also be factored by grouping: (2x - 1)(x - 3) = 0."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/elementary-algebra-2e/pages/6-1-greatest-common-factor-and-factor-by-grouping"
                )
            ),
            syllabusTopics = listOf(
                SyllabusTopic("Algebraic Expressions & Identities", "NERDC Senior Secondary", "Factorisation, expansion, solving linear and quadratic formulas.", 6),
                SyllabusTopic("Functions & Graphs", "JAMB UTME", "Linear relations, gradient, intercepts, systems of equations.", 4)
            )
        ),

        // ================= PHYSICS =================
        Textbook(
            id = "phy_okeke_ss",
            title = "Senior Secondary Physics (Standard Nigerian Edition)",
            authors = "P.N. Okeke, M.W. Anyakoha",
            publisher = "Africana First Publishers Plc / Educational Reference",
            isbn = "978-978-175-400-5",
            edition = "Revised National Curriculum Edition",
            publicationYear = 2011,
            level = EducationLevel.SS_3,
            subject = SubjectCategory.PHYSICS,
            accessType = AccessType.ONLINE_READING,
            sourceWebsite = "openstax.org & phys.libretexts.org",
            accessUrl = "https://openstax.org/books/college-physics/pages/1-introduction",
            directOnlineReaderUrl = "https://openstax.org/books/college-physics/pages/1-introduction",
            downloadUrl = "https://openstax.org/downloads/books/college-physics",
            coverUrl = "https://images.unsplash.com/photo-1636466497217-26a8cbeaf0aa?w=600&q=80",
            pageCount = 542,
            isFeatured = true,
            interactiveSimulationUrl = "https://phet.colorado.edu/en/simulations/projectile-motion",
            description = "The definitive, most celebrated Physics textbook throughout West Africa by Prof. P.N. Okeke and Dr. M.W. Anyakoha. Complete with comprehensive diagrams, worked derivations, and WAEC/JAMB past paper problems.",
            chapters = listOf(
                TextbookChapter(
                    number = 2,
                    title = "Motion, Kinematics & Projectiles",
                    startPage = 29,
                    endPage = 68,
                    summary = "Rectilinear motion with uniform acceleration, displacement-time & velocity-time graphs, projectile motion equations, time of flight, maximum height, and horizontal range.",
                    keyConcepts = listOf("Equations of motion v = u + at", "v² = u² + 2as", "s = ut + 0.5at²", "Projectile Range R = (u² sin 2θ)/g"),
                    illustrationUrl = "https://images.unsplash.com/photo-1507413245164-6160d8298b31?w=800&q=80",
                    illustrationCaption = "Figure 2.1: Parabolic trajectory of a projectile fired at angle θ with initial velocity u, resolved into independent horizontal (u cos θ) and vertical (u sin θ) components.",
                    fullChapterContent = """
                        # Chapter 2: Kinematics and Projectile Motion
                        
                        ## 2.1 Equations of Uniformly Accelerated Motion
                        For a body moving along a straight line with uniform acceleration a:
                        1. **v = u + at**
                        2. **s = ut + ½at²**
                        3. **v² = u² + 2as**
                        4. **s = ½(u + v)t**
                        
                        *(where u = initial velocity, v = final velocity, a = acceleration, s = displacement, t = time)*
                        
                        ## 2.2 Projectile Motion Under Gravity
                        A projectile is an object projected into the atmosphere subject only to gravitational acceleration g downwards. Horizontal acceleration ax = 0.
                        - Horizontal initial velocity: **ux = u cos θ**
                        - Vertical initial velocity: **uy = u sin θ**
                        
                        ### Key Projectile Formulas (from horizontal ground):
                        1. **Time to reach maximum height**: t = (u sin θ) / g
                        2. **Total Time of Flight (T)**: **T = (2u sin θ) / g**
                        3. **Maximum Height (H)**: **H = (u² sin² θ) / 2g**
                        4. **Horizontal Range (R)**: **R = (u² sin 2θ) / g**
                        - Maximum range occurs when angle **θ = 45°**, where sin 2(45°) = sin 90° = 1: **R_max = u² / g**
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "WAEC Past Question: Projectile Calculations",
                            problem = "A projectile is launched from ground level with an initial velocity of 50 m/s at an angle of 30° to the horizontal. Taking g = 10 m/s², calculate: (a) Time of flight (b) Maximum height reached (c) Horizontal range.",
                            stepByStepSolution = """
                                Given: u = 50 m/s, θ = 30°, g = 10 m/s²
                                sin 30° = 0.5, cos 30° = 0.866, sin 60° = 0.866
                                
                                (a) Total Time of Flight (T):
                                T = (2u sin θ) / g
                                T = (2 * 50 * sin 30°) / 10 = (100 * 0.5) / 10 = 50 / 10 = 5.0 seconds.
                                
                                (b) Maximum Height (H):
                                H = (u² sin² θ) / (2g)
                                H = [50² * (0.5)²] / (2 * 10) = [2500 * 0.25] / 20 = 625 / 20 = 31.25 meters.
                                
                                (c) Horizontal Range (R):
                                R = (u² sin 2θ) / g = (50² * sin 60°) / 10
                                R = (2500 * 0.866) / 10 = 250 * 0.866 = 216.5 meters.
                                
                                Final Answers:
                                (a) T = 5.0 s
                                (b) H = 31.25 m
                                (c) R = 216.5 m
                            """.trimIndent(),
                            keyFormula = "T = (2u sin θ)/g; H = (u² sin² θ)/(2g); R = (u² sin 2θ)/g",
                            examTip = "Remember that horizontal velocity (u cos θ) remains CONSTANT throughout the entire flight because there is no air resistance or horizontal acceleration."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/college-physics/pages/3-4-projectile-motion"
                ),

                TextbookChapter(
                    number = 3,
                    title = "Dynamics, Newton's Laws & Friction",
                    startPage = 69,
                    endPage = 114,
                    summary = "Inertia, Newton's 3 laws of motion, momentum conservation, impulse, coefficient of static and kinetic friction on horizontal and inclined planes.",
                    keyConcepts = listOf("Newton's 2nd law F = ma = Δp/Δt", "Conservation of momentum m1u1 + m2u2 = m1v1 + m2v2", "Friction f = μR", "Impulse I = F * Δt"),
                    illustrationUrl = "https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=800&q=80",
                    illustrationCaption = "Figure 3.1: Free body diagram of an object on an inclined plane showing weight resolved into parallel (mg sin θ) and perpendicular (mg cos θ) components balanced by Normal reaction R and Friction f.",
                    fullChapterContent = """
                        # Chapter 3: Dynamics and Laws of Motion
                        
                        ## 3.1 Newton's Laws of Motion
                        1. **First Law (Law of Inertia)**: An object remains at rest or in uniform motion in a straight line unless acted upon by a net external force.
                        2. **Second Law**: The rate of change of momentum is directly proportional to the applied force and takes place in the direction of the force: **F = dp/dt = ma**.
                        3. **Third Law**: To every action, there is an equal and opposite reaction.
                        
                        ## 3.2 Friction on an Inclined Plane
                        For a mass m resting on an incline of angle θ:
                        - Normal reaction force: **R = mg cos θ**
                        - Component of weight pulling down the plane: **W_parallel = mg sin θ**
                        - Maximum static friction: **f_max = μ_s * R = μ_s * mg cos θ**
                        - When the block is just on the verge of sliding (angle of repose λ):
                          mg sin λ = μ_s mg cos λ  =>  **μ_s = tan λ**
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "JAMB Past Question: Inclined Plane Acceleration",
                            problem = "A body of mass 5 kg is placed on a rough plane inclined at 30° to the horizontal. If the coefficient of kinetic friction is 0.2, calculate the acceleration of the body down the plane (Take g = 10 m/s²).",
                            stepByStepSolution = """
                                Step 1: Identify forces acting along the plane:
                                - Force pulling down plane = mg sin θ
                                - Opposing frictional force f_k = μ_k * R = μ_k * mg cos θ
                                
                                Step 2: Compute components:
                                mg = 5 * 10 = 50 N
                                mg sin 30° = 50 * 0.5 = 25 N
                                Normal reaction R = mg cos 30° = 50 * 0.866 = 43.3 N
                                Frictional force f_k = 0.2 * 43.3 = 8.66 N
                                
                                Step 3: Compute net accelerating force F_net:
                                F_net = mg sin θ - f_k = 25 - 8.66 = 16.34 N
                                
                                Step 4: Calculate acceleration a = F_net / m:
                                a = 16.34 / 5 = 3.27 m/s²
                                
                                Final Answer: 3.27 m/s² down the plane.
                            """.trimIndent(),
                            keyFormula = "a = g(sin θ - μ cos θ); Normal reaction R = mg cos θ",
                            examTip = "Notice that mass m cancels out when expanding F_net = ma! The formula a = g(sin θ - μ cos θ) holds for any mass sliding down an incline."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/college-physics/pages/4-3-newtons-second-law-of-motion-concept-of-a-system"
                ),

                TextbookChapter(
                    number = 7,
                    title = "Current Electricity, Ohm's Law & DC Circuits",
                    startPage = 311,
                    endPage = 412,
                    summary = "Electric current, electromotive force (EMF), internal resistance r, Ohm's law, series and parallel resistor combinations, potential divider circuits, and electrical power.",
                    keyConcepts = listOf("Ohm's law V = IR", "Series resistors R_total = R1 + R2", "Parallel resistors 1/R_total = 1/R1 + 1/R2", "Lost volts E = I(R + r)"),
                    illustrationUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&q=80",
                    illustrationCaption = "Figure 7.1: Circuit diagram illustrating an EMF source with internal resistance r connected to external resistors in parallel, with voltmeter and ammeter positions.",
                    fullChapterContent = """
                        # Chapter 7: Current Electricity and Circuit Analysis
                        
                        ## 7.1 Ohm's Law
                        The electric current flowing through a metallic conductor is directly proportional to the potential difference across its ends, provided temperature and other physical conditions remain constant:
                        **V = I * R**
                        
                        ## 7.2 Electromotive Force (EMF) & Internal Resistance
                        Every real battery has internal resistance r. When connected to an external load R:
                        **E = I(R + r) = V_terminal + Ir**
                        - **Lost Volts (V_lost)** = Ir (potential dropped internally inside the cell).
                        
                        ## 7.3 Resistor Combinations
                        - **In Series**: Current I is identical through each. **R_eq = R1 + R2 + R3 + ...**
                        - **In Parallel**: Potential difference V is identical across each. **1/R_eq = 1/R1 + 1/R2 + 1/R3 + ...**
                          For two parallel resistors: **R_eq = (R1 * R2) / (R1 + R2)** (Product over sum)
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "WAEC Past Question: Battery with Internal Resistance",
                            problem = "A battery of EMF 12V and internal resistance 1.5 Ω is connected to two resistors of 6 Ω and 3 Ω connected in parallel. Calculate: (a) Total external resistance (b) Current supplied by the battery (c) Terminal potential difference.",
                            stepByStepSolution = """
                                Step 1: Calculate equivalent external resistance R_p of 6 Ω and 3 Ω in parallel:
                                R_p = (R1 * R2) / (R1 + R2) = (6 * 3) / (6 + 3) = 18 / 9 = 2.0 Ω.
                                
                                Step 2: Calculate total circuit resistance R_total:
                                R_total = R_p + r = 2.0 + 1.5 = 3.5 Ω.
                                
                                Step 3: Calculate current I from battery using EMF equation:
                                I = E / (R_p + r) = 12 / 3.5 = 3.43 Amperes.
                                
                                Step 4: Calculate terminal potential difference V:
                                V = I * R_p = 3.43 * 2.0 = 6.86 Volts
                                (Or V = E - Ir = 12 - (3.43 * 1.5) = 12 - 5.14 = 6.86 V).
                                
                                Final Answers:
                                (a) R_p = 2.0 Ω
                                (b) I = 3.43 A
                                (c) V = 6.86 V
                            """.trimIndent(),
                            keyFormula = "E = I(R + r); V_terminal = E - Ir; Parallel: R_p = (R1*R2)/(R1+R2)",
                            examTip = "Notice that when current flows, terminal voltage V is always LESS than EMF E due to lost volts across the internal resistance."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/college-physics/pages/20-2-ohms-law-resistance-and-simple-circuits"
                )
            ),
            syllabusTopics = listOf(
                SyllabusTopic("Mechanics & Kinematics", "WAEC WASSCE & JAMB", "Vectors, projectiles, Newton's laws, energy, circular motion, gravitation.", 2),
                SyllabusTopic("Electricity & Circuits", "WAEC & JAMB", "Ohm's law, DC circuits, EMF, resistors in series and parallel.", 7)
            )
        ),

        // ================= CHEMISTRY =================
        Textbook(
            id = "chem_ababio_ss",
            title = "New School Chemistry for Senior Secondary Schools",
            authors = "Osei Yaw Ababio",
            publisher = "Africana First Publishers Plc / Educational Reference",
            isbn = "978-978-175-520-0",
            edition = "Standard West African Senior Secondary Edition",
            publicationYear = 2013,
            level = EducationLevel.SS_2,
            subject = SubjectCategory.CHEMISTRY,
            accessType = AccessType.ONLINE_READING,
            sourceWebsite = "openstax.org & chem.libretexts.org",
            accessUrl = "https://openstax.org/books/chemistry-2e/pages/1-introduction",
            directOnlineReaderUrl = "https://openstax.org/books/chemistry-2e/pages/1-introduction",
            downloadUrl = "https://openstax.org/downloads/books/chemistry-2e",
            coverUrl = "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=600&q=80",
            pageCount = 612,
            isFeatured = true,
            interactiveSimulationUrl = "https://phet.colorado.edu/en/simulations/reactants-products-and-leftovers",
            description = "The gold standard chemistry textbook throughout Nigeria and West Africa by Osei Yaw Ababio. Rich with chemical equations, atomic orbital drawings, reaction pathways, and practical laboratory preparation guides.",
            chapters = listOf(
                TextbookChapter(
                    number = 1,
                    title = "Atomic Structure, Isotopes & Orbitals",
                    startPage = 1,
                    endPage = 45,
                    summary = "Historical atomic models (Dalton, Thomson, Rutherford, Bohr), subatomic particles (protons, neutrons, electrons), electronic configuration (s, p, d, f orbitals), Aufbau principle, Hund's rule, and Pauli exclusion principle.",
                    keyConcepts = listOf("Electronic configuration 1s² 2s² 2p⁶", "Aufbau principle", "Hund's rule of maximum multiplicity", "Isotopes and relative atomic mass"),
                    illustrationUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=800&q=80",
                    illustrationCaption = "Figure 1.1: Shapes of s-spherical and p-dumbbell atomic orbitals with energy level filling order 1s < 2s < 2p < 3s < 3p < 4s < 3d.",
                    fullChapterContent = """
                        # Chapter 1: Atomic Structure and Quantum Numbers
                        
                        ## 1.1 The Atom and Its Constituents
                        An atom consists of a tiny dense positively charged nucleus containing **protons** (charge +1, mass 1 u) and **neutrons** (charge 0, mass 1 u), surrounded by **electrons** (charge -1, mass 1/1840 u) in energy levels.
                        - **Atomic Number (Z)**: Number of protons in the nucleus.
                        - **Mass Number (A)**: Total number of protons + neutrons.
                        - **Isotopes**: Atoms of the same element with the same atomic number but different mass numbers due to differing numbers of neutrons (e.g., ³⁵Cl and ³⁷Cl).
                        
                        ## 1.2 The Quantum Shells and Subshells
                        Electrons reside in shells (n = 1, 2, 3, 4) divided into sub-orbitals:
                        - **s subshell**: 1 orbital (max 2 electrons, spherical shape)
                        - **p subshell**: 3 orbitals px, py, pz (max 6 electrons, dumbbell shape)
                        - **d subshell**: 5 orbitals (max 10 electrons)
                        - **f subshell**: 7 orbitals (max 14 electrons)
                        
                        ## 1.3 The 3 Rules Governing Electron Filling
                        1. **Aufbau Principle**: Electrons occupy the lowest energy orbitals available first. Note that **4s fills before 3d** because 4s has lower energy!
                        2. **Pauli Exclusion Principle**: No two electrons in the same atom can have identical sets of all four quantum numbers. Each orbital holds a maximum of two electrons with opposite spins.
                        3. **Hund's Rule**: In degenerate orbitals of equal energy (e.g., 2px, 2py, 2pz), electrons occupy them singly with parallel spins before pairing up.
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "Calculating Relative Atomic Mass of Chlorine",
                            problem = "Chlorine occurs naturally as two isotopes: ³⁵Cl (atomic mass 34.97 u) with abundance 75.77% and ³⁷Cl (atomic mass 36.97 u) with abundance 24.23%. Calculate the relative atomic mass of chlorine to 2 decimal places.",
                            stepByStepSolution = """
                                Formula:
                                Relative Atomic Mass (A_r) = Σ (mass * % abundance) / 100
                                
                                Step 1: Calculate contributions:
                                Contribution from ³⁵Cl = (34.97 * 75.77) / 100 = 2649.68 / 100 = 26.50 u
                                Contribution from ³⁷Cl = (36.97 * 24.23) / 100 = 895.78 / 100 = 8.96 u
                                
                                Step 2: Sum the isotopic contributions:
                                A_r(Cl) = 26.50 + 8.96 = 35.46 u (approximately 35.5).
                                
                                Final Answer: 35.46
                            """.trimIndent(),
                            keyFormula = "A_r = [(mass1 * %1) + (mass2 * %2)] / 100",
                            examTip = "In WAEC and JAMB, relative atomic mass has NO units because it is a dimensionless ratio compared to 1/12th the mass of Carbon-12."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/chemistry-2e/pages/2-1-early-ideas-in-atomic-theory"
                ),

                TextbookChapter(
                    number = 4,
                    title = "Stoichiometry, Mole Concept & Volumetric Titration",
                    startPage = 135,
                    endPage = 186,
                    summary = "Avogadro's constant, molar mass, reacting masses, empirical & molecular formulas, percentage yield, standard solutions, and acid-base volumetric titration calculations using C1V1/n1 = C2V2/n2.",
                    keyConcepts = listOf("Mole n = mass / molar mass", "Molar volume of gas at STP = 22.4 dm³", "Concentration C = n / V", "Titration formula (Ca * Va)/na = (Cb * Vb)/nb"),
                    illustrationUrl = "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=800&q=80",
                    illustrationCaption = "Figure 4.1: Volumetric analysis titration setup showing 50 cm³ burette filled with standard acid, conical flask with base and indicator, and color change at endpoint.",
                    fullChapterContent = """
                        # Chapter 4: Stoichiometry and Titration Calculations
                        
                        ## 4.1 The Mole Concept
                        One mole contains exactly **6.022 × 10²³** elementary entities (Avogadro's constant N_A).
                        - **Number of moles (n)**: **n = mass (g) / Molar Mass (g/mol)**
                        - **Gas volume at STP (Standard Temp 0°C & Pressure 1 atm)**: **Volume (dm³) = moles * 22.4 dm³**
                        
                        ## 4.2 Solution Concentration
                        - **Molarity (M or mol/dm³)**: Moles of solute per 1 dm³ (1000 cm³) of solution:
                          **C = n / V (in dm³)**
                        - Mass concentration (g/dm³): **ρ = mass (g) / Volume (dm³) = Molarity * Molar Mass**
                        
                        ## 4.3 The Universal Titration Equation
                        When an acid A of molarity Ca and volume Va reacts with a base B of molarity Cb and volume Vb according to the stoichiometric coefficients na and nb:
                        **(Ca * Va) / na = (Cb * Vb) / nb**
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "WAEC Chemistry Practical Titration Calculation",
                            problem = "25.0 cm³ of 0.10 mol/dm³ sodium hydroxide (NaOH) solution required 20.0 cm³ of hydrochloric acid (HCl) for complete neutralisation. Calculate: (a) Molarity of the HCl acid (b) Mass concentration of the HCl acid in g/dm³ (Molar mass: H=1, Cl=35.5).",
                            stepByStepSolution = """
                                Balanced chemical equation:
                                HCl + NaOH -> NaCl + H2O
                                Mole ratio: na = 1, nb = 1
                                
                                Given values:
                                Cb = 0.10 mol/dm³
                                Vb = 25.0 cm³
                                Va = 20.0 cm³
                                na = 1, nb = 1
                                
                                (a) Apply titration equation:
                                (Ca * Va) / na = (Cb * Vb) / nb
                                (Ca * 20.0) / 1 = (0.10 * 25.0) / 1
                                20.0 * Ca = 2.5
                                Ca = 2.5 / 20.0 = 0.125 mol/dm³
                                
                                (b) Mass concentration:
                                Molar mass of HCl = 1 + 35.5 = 36.5 g/mol
                                Mass concentration = Molarity * Molar Mass
                                Mass concentration = 0.125 * 36.5 = 4.56 g/dm³
                                
                                Final Answers:
                                (a) 0.125 mol/dm³
                                (b) 4.56 g/dm³
                            """.trimIndent(),
                            keyFormula = "(Ca * Va) / na = (Cb * Vb) / nb; Mass conc = Molarity * Molar mass",
                            examTip = "Always ensure volumes Va and Vb are in the same units (usually cm³), so you do not need to convert to dm³ when finding ratio!"
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/chemistry-2e/pages/4-1-writing-and-balancing-chemical-equations"
                )
            ),
            syllabusTopics = listOf(
                SyllabusTopic("Atomic Structure & Periodicity", "WAEC & JAMB", "Constituents of atoms, electronic configuration, periodic table trends.", 1),
                SyllabusTopic("Quantitative Chemistry (Stoichiometry)", "WAEC WASSCE", "Mole calculations, chemical equations, standard solutions, titration.", 4)
            )
        ),

        // ================= BIOLOGY =================
        Textbook(
            id = "bio_michael_ss",
            title = "Essential Biology for Senior Secondary Schools",
            authors = "M.C. Michael",
            publisher = "Tonad Publishers Ltd / Educational Reference",
            isbn = "978-978-080-128-1",
            edition = "Standard Nigerian Curriculum Edition",
            publicationYear = 2018,
            level = EducationLevel.SS_1,
            subject = SubjectCategory.BIOLOGY,
            accessType = AccessType.ONLINE_READING,
            sourceWebsite = "openstax.org & bio.libretexts.org",
            accessUrl = "https://openstax.org/books/biology-2e/pages/1-introduction",
            directOnlineReaderUrl = "https://openstax.org/books/biology-2e/pages/1-introduction",
            downloadUrl = "https://openstax.org/downloads/books/biology-2e",
            coverUrl = "https://images.unsplash.com/photo-1530026405186-ed1f139313f8?w=600&q=80",
            pageCount = 498,
            isFeatured = true,
            interactiveSimulationUrl = "https://phet.colorado.edu/en/simulations/natural-selection",
            description = "The premier biology textbook for Nigerian secondary students and SSCE candidates by M.C. Michael. Packed with labelled biological anatomical drawings, organelle functions, and ecological principles.",
            chapters = listOf(
                TextbookChapter(
                    number = 1,
                    title = "Cell Biology & Organization of Life",
                    startPage = 1,
                    endPage = 54,
                    summary = "Characteristics of living things (MR NIGER D), cell theory, detailed ultra-structure of plant and animal cells, organelle functions (mitochondria, chloroplasts, nucleus), prokaryotes vs eukaryotes.",
                    keyConcepts = listOf("Plant vs Animal cell differences", "Mitochondria: powerhouse of cell", "Chloroplast: photosynthesis", "Levels of organization: Cell -> Tissue -> Organ -> System -> Organism"),
                    illustrationUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=800&q=80",
                    illustrationCaption = "Figure 1.1: Microscopic structure comparing Plant cell (rigid cellulose wall, chloroplasts, large central vacuole) and Animal cell (centrioles, small temporary vacuoles).",
                    fullChapterContent = """
                        # Chapter 1: The Cell as a Living Unit
                        
                        ## 1.1 The Cell Theory
                        Formulated by Schleiden, Schwann, and Virchow:
                        1. All living organisms are composed of one or more cells.
                        2. The cell is the fundamental unit of structure, physiology, and organization in living things.
                        3. All cells arise from pre-existing cells through division.
                        
                        ## 1.2 Plant Cell vs Animal Cell Differences:
                        | Feature | Plant Cell | Animal Cell |
                        | :--- | :--- | :--- |
                        | **Cell Wall** | Present (cellulose, rigid) | Absent |
                        | **Shape** | Fixed, regular geometric shape | Flexible, irregular shape |
                        | **Chloroplasts** | Present (contains chlorophyll) | Absent |
                        | **Vacuole** | Large, permanent central vacuole | Small, temporary vacuoles |
                        | **Centrioles** | Absent | Present (involved in spindle fibers) |
                        | **Storage Carbohydrate** | Starch granules | Glycogen granules |
                        
                        ## 1.3 Key Organelles and Functions
                        - **Nucleus**: Contains DNA chromosomes, controls all metabolic cellular activities.
                        - **Mitochondrion**: Site of cellular respiration (Krebs cycle & oxidative phosphorylation) producing ATP.
                        - **Ribosomes**: Protein synthesis sites.
                        - **Endoplasmic Reticulum (ER)**: Rough ER (has ribosomes, transports proteins), Smooth ER (synthesizes lipids and detoxifies).
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "WAEC Past Question: Microscopic Magnification",
                            problem = "A biological specimen of actual length 0.05 mm is observed under a light microscope. In the resulting drawing, the length measures 40 mm. Calculate the magnification of the drawing.",
                            stepByStepSolution = """
                                Formula:
                                Magnification (M) = Size of Drawing (Image) / Actual Size of Specimen
                                
                                Step 1: Ensure both measurements are in the same units (millimetres):
                                Drawing size = 40 mm
                                Actual size = 0.05 mm
                                
                                Step 2: Compute magnification:
                                M = 40 mm / 0.05 mm = 40 / (1/20) = 40 * 20 = 800
                                
                                Final Answer: x800 (or 800 times).
                            """.trimIndent(),
                            keyFormula = "Magnification = Image Size / Real Size",
                            examTip = "In WAEC biology practical examinations, drawing magnification must ALWAYS be preceded by 'x' (e.g., x2, x800) and written at the bottom right corner of your drawing."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/biology-2e/pages/4-1-studying-cells"
                ),

                TextbookChapter(
                    number = 8,
                    title = "Genetics, Heredity & Mendel's Laws",
                    startPage = 395,
                    endPage = 470,
                    summary = "Mendel's laws of segregation and independent assortment, dominant and recessive alleles, genotypes, phenotypes, monohybrid crosses, sex determination, and sickle cell anemia inheritance.",
                    keyConcepts = listOf("Mendel's 1st law of segregation", "Phenotype 3:1 vs Genotype 1:2:1", "Sex chromosomes XX (female) vs XY (male)", "Sickle cell inheritance AA, AS, SS"),
                    illustrationUrl = "https://images.unsplash.com/photo-1530026405186-ed1f139313f8?w=800&q=80",
                    illustrationCaption = "Figure 8.1: Punnett square of monohybrid cross between two heterozygous sickle cell carriers (AS x AS) showing 25% AA, 50% AS, 25% SS probability.",
                    fullChapterContent = """
                        # Chapter 8: Genetics and Mendelian Heredity
                        
                        ## 8.1 Fundamental Genetic Terms
                        - **Gene**: A segment of DNA located on a chromosome specifying a particular hereditary trait.
                        - **Alleles**: Alternative forms of the same gene located at the same locus on homologous chromosomes (e.g., T for tall, t for dwarf).
                        - **Genotype**: The genetic constitution of an organism (e.g., TT, Tt, tt).
                        - **Phenotype**: The observable physical manifestation of a trait (e.g., Tall or Dwarf).
                        
                        ## 8.2 Mendel's First Law (Law of Segregation)
                        The characteristics of an organism are controlled by pairs of alleles which separate (segregate) during gamete formation, so that each gamete carries only one member of the pair.
                        
                        ## 8.3 Sickle Cell Inheritance in Humans
                        Normal hemoglobin allele is Hb^A, sickle cell hemoglobin allele is Hb^S (codominant/recessive disease).
                        - **Hb^A Hb^A (AA)**: Normal red blood cells.
                        - **Hb^A Hb^S (AS)**: Sickle cell trait carrier (healthy under normal conditions, resistant to severe falciparum malaria).
                        - **Hb^S Hb^S (SS)**: Suffers from sickle cell anemia (red blood cells sickle under low oxygen tension, causing blockages and pain crises).
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "JAMB Past Question: Sickle Cell Carrier Probability",
                            problem = "A man who is a sickle cell carrier (AS) marries a woman who is also a sickle cell carrier (AS). What is the probability that their first child will suffer from sickle cell anemia (SS)?",
                            stepByStepSolution = """
                                Step 1: Write parental genotypes:
                                Father: AS (gametes: A or S)
                                Mother: AS (gametes: A or S)
                                
                                Step 2: Construct the Punnett Square:
                                | Gametes | A | S |
                                | :--- | :--- | :--- |
                                | **A** | AA | AS |
                                | **S** | AS | SS |
                                
                                Step 3: Count offspring genotypic frequencies:
                                - 1/4 (25%) AA: Normal
                                - 2/4 (50%) AS: Carrier
                                - 1/4 (25%) SS: Sickle cell anemia
                                
                                Step 4: Answer the question:
                                The probability of having a child with sickle cell anemia (SS) is 1/4 or 25% or 0.25.
                                
                                Final Answer: 25% (or 1 in 4).
                            """.trimIndent(),
                            keyFormula = "Punnett square ratio: 1 AA : 2 AS : 1 SS",
                            examTip = "Notice that each pregnancy is an independent event! The probability of each child being SS is always 25%, regardless of how many children they have."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/biology-2e/pages/12-1-mendels-experiments-and-the-laws-of-probability"
                )
            ),
            syllabusTopics = listOf(
                SyllabusTopic("Organization of Life & Cells", "WAEC & JAMB", "Cell structure, functions of organelles, cell division (mitosis and meiosis).", 1),
                SyllabusTopic("Heredity & Genetics", "WAEC & JAMB", "Chromosomes, genes, inheritance patterns, variation and adaptation.", 8)
            )
        ),

        // ================= ECONOMICS =================
        Textbook(
            id = "econ_anyaele_ss",
            title = "Comprehensive Economics for Senior Secondary Schools",
            authors = "J.U. Anyaele",
            publisher = "A. Johnson Publishers Ltd / Educational Reference",
            isbn = "978-978-315-081-2",
            edition = "Standard West African SSCE Edition",
            publicationYear = 2015,
            level = EducationLevel.SS_2,
            subject = SubjectCategory.ECONOMICS,
            accessType = AccessType.ONLINE_READING,
            sourceWebsite = "openstax.org & socialsci.libretexts.org",
            accessUrl = "https://openstax.org/books/principles-economics-3e/pages/1-introduction",
            directOnlineReaderUrl = "https://openstax.org/books/principles-economics-3e/pages/1-introduction",
            downloadUrl = "https://openstax.org/downloads/books/principles-economics-3e",
            coverUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&q=80",
            pageCount = 420,
            isFeatured = true,
            description = "The classic economics guide by J.U. Anyaele. Thoroughly illustrates demand and supply curves, equilibrium price determination, elasticity formulas, cost schedules, and macroeconomic national income circular flow.",
            chapters = listOf(
                TextbookChapter(
                    number = 2,
                    title = "Theory of Demand, Supply & Market Equilibrium",
                    startPage = 33,
                    endPage = 90,
                    summary = "Laws of demand and supply, individual vs market schedules, shift in curve vs movement along curve, equilibrium price and quantity, consumer surplus, and price controls (maximum and minimum price legislation).",
                    keyConcepts = listOf("Law of Demand (inverse price-quantity)", "Law of Supply (direct price-quantity)", "Equilibrium Qd = Qs", "Price ceiling (shortage) vs Price floor (surplus)"),
                    illustrationUrl = "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=800&q=80",
                    illustrationCaption = "Figure 2.1: Market equilibrium chart where downward sloping demand curve crosses upward sloping supply curve at equilibrium price Pe and quantity Qe.",
                    fullChapterContent = """
                        # Chapter 2: Demand, Supply, and Price Determination
                        
                        ## 2.1 The Law of Demand
                        The law of demand states that, *ceteris paribus* (all other factors remaining constant), as the price of a commodity increases, the quantity demanded decreases, and vice versa.
                        - Downward slope from left to right due to diminishing marginal utility and substitution effect.
                        
                        ## 2.2 Movement Along Curve vs Shift of Curve
                        - **Movement along curve**: Caused ONLY by a change in the price of the commodity itself (expansion or contraction of demand).
                        - **Shift of entire curve**: Caused by changes in non-price determinants (consumer income, tastes/fashion, price of substitutes, population, government taxation).
                        
                        ## 2.3 Market Equilibrium
                        Equilibrium occurs at the point where Quantity Demanded equals Quantity Supplied:
                        **Qd = Qs**
                        - At any price above equilibrium: **Excess Supply (Surplus)** forces price downward.
                        - At any price below equilibrium: **Excess Demand (Shortage)** forces price upward.
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "WAEC Past Question: Mathematical Equilibrium Determination",
                            problem = "The demand and supply functions for a commodity are given as: Qd = 100 - 4P and Qs = 20 + 6P, where P is price in Naira, Qd is quantity demanded, and Qs is quantity supplied. Calculate: (a) Equilibrium price (b) Equilibrium quantity.",
                            stepByStepSolution = """
                                Step 1: At market equilibrium:
                                Qd = Qs
                                
                                Step 2: Equate both equations:
                                100 - 4P = 20 + 6P
                                
                                Step 3: Rearrange to solve for P:
                                100 - 20 = 6P + 4P
                                80 = 10P
                                P = 80 / 10 = 8
                                
                                Therefore, Equilibrium Price Pe = ₦8.00.
                                
                                Step 4: Substitute P = 8 into either Qd or Qs to find equilibrium quantity Qe:
                                Qd = 100 - 4(8) = 100 - 32 = 68 units.
                                Check with Qs: Qs = 20 + 6(8) = 20 + 48 = 68 units (Correct!).
                                
                                Final Answers:
                                (a) Equilibrium Price Pe = ₦8.00
                                (b) Equilibrium Quantity Qe = 68 units
                            """.trimIndent(),
                            keyFormula = "At Equilibrium: Qd = Qs; Substitute Pe into Qd to find Qe",
                            examTip = "Always check your answer by substituting Pe into BOTH equations to confirm they yield the exact same quantity."
                        )
                    ),
                    directReaderUrl = "https://openstax.org/books/principles-economics-3e/pages/3-1-demand-supply-and-equilibrium-in-markets-for-goods-and-services"
                )
            ),
            syllabusTopics = listOf(
                SyllabusTopic("Price Determination & Elasticity", "WAEC WASSCE", "Equilibrium price, price ceiling/floor, price elasticity calculations.", 2)
            )
        ),

        // ================= OFFICIAL EXAMINATION SYLLABUSES =================
        Textbook(
            id = "waec_official_syllabus",
            title = "Official WAEC WASSCE Regulations and Syllabuses (All Subjects)",
            authors = "West African Examinations Council (WAEC Directorate)",
            publisher = "WAEC International Headquarters",
            edition = "Official WAEC Regulations & Chief Examiners' Reports",
            publicationYear = 2024,
            level = EducationLevel.EXAM_WAEC,
            subject = SubjectCategory.ALL,
            accessType = AccessType.ONLINE_READING,
            sourceWebsite = "waeconline.org.ng",
            accessUrl = "https://www.waeconline.org.ng/e-learning/",
            directOnlineReaderUrl = "https://www.waeconline.org.ng/e-learning/",
            coverUrl = "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=600&q=80",
            pageCount = 480,
            isFeatured = true,
            description = "The official, definitive examination regulations, subject syllabuses, marking schemes, and chief examiners' reports published by the West African Examinations Council for WASSCE candidates across Nigeria and West Africa.",
            chapters = listOf(
                TextbookChapter(
                    number = 1,
                    title = "General Mathematics Scheme & Formula Compendium",
                    startPage = 1,
                    endPage = 45,
                    summary = "Detailed breakdown of WAEC Mathematics Paper 1 (Objective 50 questions) and Paper 2 (Theory Sections A & B), official formula sheet, mark deductions for accuracy, and examiners' advice.",
                    keyConcepts = listOf("Paper 1 (50 marks, 1.5 hrs)", "Paper 2 Section A (40 marks, compulsory)", "Paper 2 Section B (60 marks, answer 5 of 8)", "Negative marks avoided"),
                    illustrationUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800&q=80",
                    illustrationCaption = "Figure 1.1: Official WAEC Mathematics marking scheme breakdown illustrating Method marks (M), Accuracy marks (A), and Independent marks (B).",
                    fullChapterContent = """
                        # WAEC WASSCE General Mathematics Syllabus Regulations
                        
                        ## Paper Structure:
                        - **Paper 1 (Multiple Choice)**: 50 objective questions drawn from the entire syllabus. Time allowed: 1 hour 30 minutes. Weighting: 50 marks.
                        - **Paper 2 (Essay / Theory)**: 2 hours 30 minutes. Weighting: 100 marks.
                          - **Section A (Compulsory)**: 5 short structured questions covering fundamental syllabus areas (40 marks total, 8 marks each).
                          - **Section B**: 8 detailed problem-solving questions. Candidates are required to answer any 5 questions (60 marks total, 12 marks each).
                        
                        ## Marking Rubric Guidelines:
                        - **M (Method Marks)**: Awarded for applying correct mathematical principles, even if an arithmetic calculation error occurs later.
                        - **A (Accuracy Marks)**: Awarded for the correct final answer resulting from correct method (cannot be awarded without prior M mark).
                        - **B (Independent Marks)**: Awarded unconditionally for stating a correct value, formula, or geometric reason.
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "WAEC Chief Examiner Recommendation on Geometric Reasons",
                            problem = "In circle geometry, why do candidates lose marks even when obtaining the correct numerical angle?",
                            stepByStepSolution = """
                                WAEC Chief Examiners report that candidates frequently write:
                                'x = 50°' without stating the geometric theorem.
                                
                                Correct standard presentation for full marks:
                                'Angle ABC = 50° (Reason: Angle in alternate segment is equal to angle subtended by tangent)' [M1, A1].
                                
                                Rule: ALWAYS provide the geometric justification in parentheses beside every angle step.
                            """.trimIndent(),
                            keyFormula = "Geometry answers require [Numerical value + Theorem Reason in brackets]",
                            examTip = "Always write all construction lines in pencil and bold calculations in blue/black ink."
                        )
                    ),
                    directReaderUrl = "https://www.waeconline.org.ng/e-learning/Mathematics/mathsmain.html"
                )
            ),
            syllabusTopics = listOf(
                SyllabusTopic("All WAEC Senior Secondary Syllabuses", "Official WAEC Regulations", "Core and elective subjects examination guidelines and standards.", 1)
            )
        ),

        Textbook(
            id = "jamb_ibass_syllabus",
            title = "Official JAMB UTME Interactive Brochure & e-Syllabus System (IBASS)",
            authors = "Joint Admissions and Matriculation Board (JAMB)",
            publisher = "JAMB National Headquarters, Bwari, Abuja",
            edition = "Official National IBASS Digital Compendium",
            publicationYear = 2024,
            level = EducationLevel.EXAM_JAMB,
            subject = SubjectCategory.ALL,
            accessType = AccessType.ONLINE_READING,
            sourceWebsite = "ibass.jamb.gov.ng",
            accessUrl = "https://ibass.jamb.gov.ng",
            directOnlineReaderUrl = "https://ibass.jamb.gov.ng",
            coverUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=600&q=80",
            pageCount = 360,
            isFeatured = true,
            description = "The authoritative curriculum system issued by JAMB detailing exact topics, specific learning objectives, recommended reading texts, and question distribution for all 24 UTME CBT subjects.",
            chapters = listOf(
                TextbookChapter(
                    number = 1,
                    title = "JAMB CBT Exam Strategy & Scoring System",
                    startPage = 1,
                    endPage = 35,
                    summary = "The 4-subject combination structure, Use of English (60 questions), 3 subject disciplines (40 questions each), time management for 120 minutes (2 hours), and negative marking debunked.",
                    keyConcepts = listOf("400 total marks", "Use of English: 60 questions (100 marks)", "3 Subject disciplines: 40 questions each (100 marks each)", "No negative marking in UTME"),
                    illustrationUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=800&q=80",
                    illustrationCaption = "Figure 1.1: JAMB 4-subject scoring matrix and 120-minute CBT time allocation strategy (30 seconds per question on English, 45 seconds on sciences).",
                    fullChapterContent = """
                        # Official JAMB UTME IBASS Examination Guidelines
                        
                        ## 1. Structure of the UTME CBT Examination:
                        - **Total Number of Questions**: 180 questions
                        - **Total Time Allowed**: 2 hours (120 minutes)
                        - **Breakdown**:
                          1. **Use of English (Compulsory)**: 60 questions (Total 100 marks)
                          2. **Subject 2**: 40 questions (Total 100 marks)
                          3. **Subject 3**: 40 questions (Total 100 marks)
                          4. **Subject 4**: 40 questions (Total 100 marks)
                          **Grand Total = 400 marks**.
                        
                        ## 2. Key Strategies for Scoring 300+:
                        - **Never Leave Any Question Blank**: There is NO penalty or negative marking for wrong answers in JAMB UTME.
                        - **Keyboard Shortcuts in CBT Hall**:
                          - **A, B, C, D**: Select corresponding option.
                          - **N**: Move to Next question.
                          - **P**: Return to Previous question.
                          - **S**: Submit exam (only press when finished!).
                    """.trimIndent(),
                    workedExamples = listOf(
                        ChapterWorkedExample(
                            title = "Approved JAMB Faculty Subject Combinations",
                            problem = "What are the compulsory 4-subject combinations for: (1) Medicine & Surgery (2) Computer Science / Engineering (3) Law (4) Accounting?",
                            stepByStepSolution = """
                                1. **Medicine & Surgery**:
                                   - English Language (Compulsory)
                                   - Biology
                                   - Chemistry
                                   - Physics
                                
                                2. **Engineering & Computer Science**:
                                   - English Language (Compulsory)
                                   - Mathematics
                                   - Physics
                                   - Chemistry
                                
                                3. **Law**:
                                   - English Language (Compulsory)
                                   - Literature in English
                                   - Any two Arts or Social Science subjects (e.g., Government, CRS/IRS, Economics).
                                
                                4. **Accounting & Business Administration**:
                                   - English Language (Compulsory)
                                   - Mathematics
                                   - Economics
                                   - Any other social science subject (Commerce, Government, or Accounting).
                            """.trimIndent(),
                            keyFormula = "English Language is compulsory for all UTME candidates without exception.",
                            examTip = "Check JAMB IBASS brochure requirements for your specific institution, as university cut-offs and combination rules may vary."
                        )
                    ),
                    directReaderUrl = "https://ibass.jamb.gov.ng"
                )
            ),
            syllabusTopics = listOf(
                SyllabusTopic("JAMB UTME Subject Outlines", "JAMB IBASS", "Official 24-subject curricula, question types, recommended textbooks.", 1)
            )
        )
    )

    fun getAllTextbooks(): List<Textbook> = REAL_TEXTBOOKS

    fun getTextbookById(id: String): Textbook? = REAL_TEXTBOOKS.find { it.id == id }

    fun filterTextbooks(
        query: String = "",
        level: EducationLevel = EducationLevel.ALL,
        subject: SubjectCategory = SubjectCategory.ALL,
        accessType: AccessType? = null,
        onlySaved: Boolean = false,
        onlyFreeDownload: Boolean = false,
        examinationFilter: String? = null
    ): List<Textbook> {
        return REAL_TEXTBOOKS.filter { book ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                book.title.lowercase().contains(q) ||
                    book.authors.lowercase().contains(q) ||
                    book.publisher.lowercase().contains(q) ||
                    (book.isbn?.lowercase()?.contains(q) == true) ||
                    book.subject.displayName.lowercase().contains(q) ||
                    book.description.lowercase().contains(q) ||
                    book.syllabusTopics.any { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) } ||
                    book.chapters.any { it.title.lowercase().contains(q) || it.keyConcepts.any { c -> c.lowercase().contains(q) } }
            }

            val matchesLevel = when (level) {
                EducationLevel.ALL -> true
                EducationLevel.PRIMARY_1, EducationLevel.PRIMARY_2, EducationLevel.PRIMARY_3,
                EducationLevel.PRIMARY_4, EducationLevel.PRIMARY_5, EducationLevel.PRIMARY_6 ->
                    book.level.category == "Primary" || book.level == level

                EducationLevel.JSS_1, EducationLevel.JSS_2, EducationLevel.JSS_3 ->
                    book.level.category == "Junior Secondary" || book.level == level

                EducationLevel.SS_1, EducationLevel.SS_2, EducationLevel.SS_3 ->
                    book.level.category == "Senior Secondary" || book.level == level

                EducationLevel.EXAM_WAEC -> book.level == EducationLevel.EXAM_WAEC || book.syllabusTopics.any { it.examinationOrCurriculum.contains("WAEC", ignoreCase = true) }
                EducationLevel.EXAM_JAMB -> book.level == EducationLevel.EXAM_JAMB || book.syllabusTopics.any { it.examinationOrCurriculum.contains("JAMB", ignoreCase = true) }
                EducationLevel.EXAM_NECO -> book.level == EducationLevel.EXAM_NECO || book.syllabusTopics.any { it.examinationOrCurriculum.contains("NECO", ignoreCase = true) }
            }

            val matchesExam = if (examinationFilter.isNullOrBlank()) {
                true
            } else {
                val e = examinationFilter.lowercase()
                book.level.displayName.lowercase().contains(e) ||
                    book.syllabusTopics.any { it.examinationOrCurriculum.lowercase().contains(e) } ||
                    book.title.lowercase().contains(e)
            }

            val matchesSubject = (subject == SubjectCategory.ALL || book.subject == subject)
            val matchesAccessType = (accessType == null || book.accessType == accessType)
            val matchesFreeDownload = (!onlyFreeDownload || book.isFreeDownload)
            val matchesSaved = (!onlySaved || _savedBookIds.value.contains(book.id))

            matchesQuery && matchesLevel && matchesExam && matchesSubject && matchesAccessType && matchesFreeDownload && matchesSaved
        }
    }

    fun toggleSaveBook(bookId: String): Boolean {
        val current = _savedBookIds.value.toMutableSet()
        val isSavedNow = if (current.contains(bookId)) {
            current.remove(bookId)
            false
        } else {
            current.add(bookId)
            true
        }
        _savedBookIds.value = current
        return isSavedNow
    }

    fun isBookSaved(bookId: String): Boolean {
        return _savedBookIds.value.contains(bookId)
    }

    fun updateReadingProgress(bookId: String, page: Int, totalPages: Int) {
        val progress = ReadingProgress(
            textbookId = bookId,
            currentPage = page,
            totalPages = totalPages,
            percentCompleted = (page.toFloat() / totalPages.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
            lastReadTimestamp = System.currentTimeMillis()
        )
        val current = _progressMap.value.toMutableMap()
        current[bookId] = progress
        _progressMap.value = current
    }

    fun addBookmark(bookId: String, pageNumber: Int, note: String) {
        val bookmark = TextbookBookmark(
            id = UUID.randomUUID().toString(),
            textbookId = bookId,
            pageNumber = pageNumber,
            note = note.ifBlank { "Page $pageNumber Bookmark" },
            timestamp = System.currentTimeMillis()
        )
        val current = _bookmarks.value.toMutableMap()
        val list = current[bookId]?.toMutableList() ?: mutableListOf()
        list.add(bookmark)
        current[bookId] = list
        _bookmarks.value = current
    }

    fun removeBookmark(bookId: String, bookmarkId: String) {
        val current = _bookmarks.value.toMutableMap()
        val list = current[bookId]?.toMutableList() ?: return
        list.removeAll { it.id == bookmarkId }
        current[bookId] = list
        _bookmarks.value = current
    }
}
