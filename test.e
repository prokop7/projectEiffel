class
	APPLICATION

inherit
	ARGUMENTS

create
	make

feature
		-- Run application
	make
		local
			INTEGER : a, c, b
			STRING : t
		do
			b := 52345;
			print("World!" + a.out + "gfhgf" + 5.out);
			print(b.out);
			t := lol();
			print(t);
		end

	lol : STRING
		local
			INTEGER : a
		do
			a := 1;
			print(a.out);
			result:= "lol";
		end

	eiffel : INTEGER
		local
		do
			result:= 0;
		end
end