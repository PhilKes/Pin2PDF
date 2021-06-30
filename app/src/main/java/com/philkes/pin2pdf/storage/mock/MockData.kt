package com.philkes.pin2pdf.storage.mock

import com.philkes.pin2pdf.model.PinModel
import java.util.*

object MockData {
    var MockPins: List<PinModel> = ArrayList(
        Arrays.asList(
            PinModel(
                "HONIG-BALSAMICO-KNOBLAUCH-PILZE",
                "https://i.pinimg.com/564x/45/8e/b1/458eb1bd5362bd31a81e118816a62ffb.jpg",
                "https://holabys.com/2020/08/14/honig-balsamico-knoblauch-pilze/",
                null, "Test", ""
            ),
            PinModel(
                "Schwäbischer Kartoffelsalat",
                "https://i.pinimg.com/236x/df/00/4d/df004dfb084003188908ab9ee3d5b3eb.jpg",
                "https://holabys.com/2020/08/14/honig-balsamico-knoblauch-pilze/",
                null, "Test", ""
            ),
            PinModel(
                "Rezept für Kartoffel-Zucchini-Puffer",
                "https://i.pinimg.com/564x/35/39/73/3539736e272c25216f63c3e951144bbb.jpg",
                "https://www.freundin.de/kochen-diaet-kartoffel-zucchini-puffer?utm_medium=social&utm_campaign=Sharing&utm_source=Sharing_Pinterest",
                null, "Test", ""
            )
        )
    )
}