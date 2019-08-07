// 这个是打包之后jar包的名字
jarName in assembly := "apss-assembly-1.0.jar"

// 这个作用是在打包的时候，跳过测试
test in assembly := {}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
