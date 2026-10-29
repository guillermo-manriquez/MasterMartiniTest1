package com.example.mastermartini.model

/**
 * Public Master Martini Chile details, taken from https://mastermartini.cl/contacto/
 * (reviewed and approved on 2026-10-29). The WhatsApp number comes from the site's
 * WhatsApp button link.
 */
object CompanyInfo {
    const val WEBSITE_URL = "https://mastermartini.cl/"

    const val EMAIL = "clientes@mastermartini.cl"

    const val PHONE_DISPLAY = "+56 2 2661 1700"
    const val PHONE_DIAL = "+56226611700"

    const val WHATSAPP_DISPLAY = "+56 9 4457 0770"
    /** International format without "+", as wa.me expects. */
    const val WHATSAPP_NUMBER = "56944570770"

    const val ADDRESS = "Avenida Américo Vespucio 1385, módulo 3, Centro de empresas Spacioflex, Quilicura, Santiago"
    const val OPENING_HOURS = "Lunes a viernes, 9:00 a 18:00 horas"
}
