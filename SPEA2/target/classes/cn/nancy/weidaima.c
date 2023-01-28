// 配置编码
初始化当前编码操作 X* <- []
初始化编码 Code <- []
初始化未编码的操作集合 UncodedProcess <- all peocesses
while UncodedProcess.length() != 0 do
	// 寻找是否有仅有一个可选RMT的操作
	for eachprocess in MachineSet do
        if eachprocess.length() == 1 then
			X* <- eachprocess
			goto line(19)
		end if
	end for
	// 找是否有无交集的相邻操作
	for eachprocess in MachineSet do
		if eachprocess.isNoIntersection(eachprocess.next()) then
			X* <- {eachprocess,eachprocess.next()}
		else
			X* <- random select from UncodedProcess
		end if
	end for
	Code[X*] <- random select from MachineSet[eachprocess] base on rule
	// 更新记录
	delete X* from UncodedProcess
	for eachmachine in MachineSet[X*] do
		if eachmachine != Code[X*] then
			delete eachmachine
		end if
	end for
	for eachprocess in MachineSet do
		// 向左搜索，直到找到第一个包含 Code[X*] 但不连续且未编码的操作 j
		for i <- (eachprocess.index-1) to 0 do
			if ! MachineSet[i].contains(Code[X*]) then
				for j <- (i-1) to 0 do
					delete Code[X*] from MachineSet[j]
				end for
			end if
		end for
		// 向右搜索，直到找到第一个包含 Code[X*] 但不连续且未编码的操作 j
		for i <- (eachprocess.index+1) to MachineSet.length() do
			if ! MachineSet[i].contains(Code[X*]) then
				for j <- (i+1) to MachineSet.length() do
					delete Code[X*] from MachineSet[j]
				end for
			end if
		end for
	end for
	X* <- []
	// 判断是否有未选择机器但RMT集合为空的操作
	for eachprocess in MachineSet do
		if MachineSet[eachmachine].isEmpty() then
			goto line(1)
		end if
	end for
	// 判断是否存在选择相同机器的不同操作
	for eachprocess in MachineSet do
		if ! Code[eachprocess].isEmpty() then
			for i <- (eachprocess.index+1) to MachineSet.length() do
				if Code[i] == Code[eachprocess] then
					if Code[j <- (eachprocess.index + 1):(i-1)].isEmpty() && MachineSet[j].contains(Code[eachprocess]) then
						Code[(eachprocess.index + 1):(i-1)] <- Code[eachprocess]
						X* <- {(eachprocess.index + 1):(i-1)}
					elseif Code[i] || Code[eachprocess] can be replaced then
						replaced
						X* <- (Code[i] || Code[eachprocess])
					elseif 
						goto line(1)
					end if
					goto line(20)
				end if
			end for
		end if
	end for
end while

// 操作编码
初始化当前编码操作 Y* <- []
初始化未编码的操作集合 UncodedProcess <- []
初始化编码 Code <- []
初始化作业序号 job <- 1
while job <= Process.length() do
	UncodedProcess <- Process[job]
	// 找出作业 job 的每个操作的可用工位
	OptStageSet <- []
	for i <- 1 to Process[job].length() do
		OptStageSet[i] <- StageSet[Process[job]]
		if i <= Process[job].length() then
			delete those > ProcessSet.length() from OptStageSet[i]
		else
			delete those <= 1 from OptStageSet[i]
		end if
	end for
	// 寻找是否有仅有一个可选工位的操作
	for eachprocess in OptStageSet do
		if eachprocess.length() == 1 then
			Y* <- eachprocess
			Code[Y*] <- OptStageSet[eachprocess]
			goto line(xx)
		end if
	end for
	// 随机选择操作
	Y* <- random select from UncodedProcess
	Code[Y*] <- random select from OptStageSet[eachprocess]
	// 更新记录
	delete Y* from UncodedProcess
	for eachprocess in OptStageSet do
		if eachprocess < Y* then
			delete those > Code[Y*] from OptStageSet[eachprocess]
		elseif eachprocess > Y* then
			delete those < Code[Y*] from OptStageSet[eachprocess]
		end if
	end for
	// 判断是否有未选择工位但工位集合为空的操作
	for eachprocess in OptStageSet do
		if OptStageSet[eachprocess].isEmpty() then
			goto line(xx)
		end if
	end for
	// 判断是否作业 job 的所有操作均选择完工位
	for eachprocess in OptStageSet do
		if Code[eachprocess].isEmpty() then
			goto line(xx)
		end if
	end for
	job++
end while

// VNS伪代码
for x in Pop do
	// 扰动算子
	N <- x的邻域
	// VND
	for i <- 1 to N.length() do
		x'' <- random select from N[i]
		N'' <- x''的邻域
		for j <- 1 to N''.length() do
			x'''' <- N''[j]的最优解
			if x'''' 支配 x'' then
				x'' <- x''''
				N'' <- x''的邻域
				j <- 1
			end if
		end for
		if x'' 支配 x then
			x <- x''
		end if
	end for
end for