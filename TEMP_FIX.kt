    
    private fun generateHistoricalFearGreed(currentValue: Int, daysOffset: Int): Int {
        // Generate realistic historical values based on current value
        // Fear & Greed tends to have some volatility but not extreme swings day to day
        val baseVariation = when {
            daysOffset == -1 -> (-3..3).random() // Yesterday: small variation
            daysOffset <= -7 -> (-8..8).random() // Last week: moderate variation
            else -> (-5..5).random()
        }
        
        val historicalValue = currentValue + baseVariation
        return historicalValue.coerceIn(0, 100)
    }
}