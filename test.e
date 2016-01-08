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
			INTEGER : a, c, a_lot_of_string, b, i
			STRING : t
		do
			b := 52345;
			print("World!" + a_lot_of_string.out + "gfhgf" + 5.out);
			print(b.out);
			-- Here will be loop
				print(i.out);
			t := lol();
			print(t.out);
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