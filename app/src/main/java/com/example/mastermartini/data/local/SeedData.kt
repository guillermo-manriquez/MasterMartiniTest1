package com.example.mastermartini.data.local

import com.example.mastermartini.data.local.entity.ProductEntity
import com.example.mastermartini.model.ProductCategory.FOOD_SERVICE
import com.example.mastermartini.model.ProductCategory.GELATERIA
import com.example.mastermartini.model.ProductCategory.PANADERIA_PASTELERIA
import com.example.mastermartini.model.ProductCategory.RETAIL

/**
 * Initial catalog inserted when the database is created.
 * Names follow the Master Martini product families; descriptions and
 * presentations are written for this academic project, not copied from the site.
 */
object SeedData {

    // Demo account for the evaluation (test credentials, also listed in the README).
    const val DEMO_NAME = "Usuario Demo"
    const val DEMO_EMAIL = "demo@mastermartini.cl"
    const val DEMO_PHONE = "+56 9 1234 5678"
    const val DEMO_PASSWORD = "Demo2026"

    val products = listOf(
        // Panadería y Pastelería
        ProductEntity(
            id = 1, name = "Mantequilla laminada", family = "Mantequilla", category = PANADERIA_PASTELERIA,
            description = "Mantequilla en lámina de alto punto de fusión, pensada para croissants y masas hojaldradas con capas bien definidas.",
            presentation = "Caja 10 kg (5 láminas de 2 kg)",
        ),
        ProductEntity(
            id = 2, name = "Margarina para hojaldre", family = "Margarina", category = PANADERIA_PASTELERIA,
            description = "Margarina vegetal de textura plástica que facilita el laminado y da un volumen estable al hojaldre.",
            presentation = "Caja 10 kg",
        ),
        ProductEntity(
            id = 3, name = "Melange para masas", family = "Melange", category = PANADERIA_PASTELERIA,
            description = "Mezcla de mantequilla y grasa vegetal que combina sabor lácteo con buena trabajabilidad en masas dulces.",
            presentation = "Caja 10 kg",
        ),
        ProductEntity(
            id = 4, name = "Crema vegetal para batir", family = "Cremas vegetales", category = PANADERIA_PASTELERIA,
            description = "Crema de origen vegetal que monta con firmeza y mantiene la forma en rellenos y decoraciones.",
            presentation = "Caja 12 x 1 L",
        ),
        ProductEntity(
            id = 5, name = "Mix bizcochuelo", family = "Mix pastelería", category = PANADERIA_PASTELERIA,
            description = "Premezcla completa para bizcochos esponjosos; solo requiere agregar huevos y agua.",
            presentation = "Saco 10 kg",
        ),
        ProductEntity(
            id = 6, name = "Mix pan integral", family = "Mix panificación", category = PANADERIA_PASTELERIA,
            description = "Premezcla con harina integral y semillas para panes de miga húmeda y corteza crujiente.",
            presentation = "Saco 25 kg",
        ),
        ProductEntity(
            id = 7, name = "Glaseado espejo chocolate", family = "Glaseados espejo", category = PANADERIA_PASTELERIA,
            description = "Cobertura brillante lista para usar sobre tortas y entremets congelados.",
            presentation = "Balde 5 kg",
        ),
        ProductEntity(
            id = 8, name = "Glaseado espejo frutilla", family = "Glaseados espejo", category = PANADERIA_PASTELERIA,
            description = "Glaseado con acabado espejo, sabor y color frutilla, para postres fríos.",
            presentation = "Balde 5 kg",
        ),
        // Gelatería
        ProductEntity(
            id = 9, name = "Base para helado de crema", family = "Bases para helado", category = GELATERIA,
            description = "Base en polvo balanceada en azúcares y estabilizantes para helados de crema de textura suave.",
            presentation = "Bolsa 2 kg",
        ),
        ProductEntity(
            id = 10, name = "Pasta de avellana", family = "Avellanas", category = GELATERIA,
            description = "Pasta de avellanas tostadas que aporta sabor intenso a helados, rellenos y cremas.",
            presentation = "Balde 3 kg",
        ),
        ProductEntity(
            id = 11, name = "Pasta de pistacho", family = "Pastas saborizantes", category = GELATERIA,
            description = "Pasta de pistacho para helados y rellenos con color natural.",
            presentation = "Balde 3 kg",
        ),
        ProductEntity(
            id = 12, name = "Cobertura de chocolate", family = "Coberturas", category = GELATERIA,
            description = "Cobertura que forma una capa crocante al contacto con el helado.",
            presentation = "Balde 4 kg",
        ),
        // Food Service
        ProductEntity(
            id = 13, name = "Base para panna cotta", family = "Bases para postres", category = FOOD_SERVICE,
            description = "Preparación en polvo para panna cotta cremosa; se disuelve en leche o crema caliente.",
            presentation = "Bolsa 1 kg",
        ),
        ProductEntity(
            id = 14, name = "Mix mousse de chocolate", family = "Mix pastelería", category = FOOD_SERVICE,
            description = "Premezcla para un mousse aireado y estable, ideal para servicio en porciones.",
            presentation = "Bolsa 1 kg",
        ),
        ProductEntity(
            id = 15, name = "Gelatina neutra", family = "Gelatina", category = FOOD_SERVICE,
            description = "Gelatina neutra de uso en frío que da brillo y protege la fruta en tartas y postres.",
            presentation = "Balde 6 kg",
        ),
        // Retail
        ProductEntity(
            id = 16, name = "Crema vegetal 1 L", family = "Cremas vegetales", category = RETAIL,
            description = "Crema vegetal para batir en un formato práctico para pastelerías pequeñas y venta minorista.",
            presentation = "Caja 1 L",
        ),
        ProductEntity(
            id = 17, name = "Mix brownie", family = "Mix pastelería", category = RETAIL,
            description = "Premezcla para brownies húmedos con trozos de chocolate.",
            presentation = "Bolsa 500 g",
        ),
        ProductEntity(
            id = 18, name = "Avellanas tostadas", family = "Avellanas", category = RETAIL,
            description = "Avellanas enteras tostadas, listas para decorar o incorporar a masas.",
            presentation = "Bolsa 1 kg",
        ),
    )
}
