package com.projectkorra.projectkorra.configuration

import com.projectkorra.projectkorra.configuration.water.IceWave
import com.projectkorra.projectkorra.configuration.water.WaterSpout

interface Water {
    val WaterSpout: WaterSpout
    val IceWave: IceWave
}