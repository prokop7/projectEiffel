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
			INTEGER : a, c, a_lot_of_string, b, p, i
			STRING : t
		do
			b := 52345;
			p := 0;
			print("World!" + a_lot_of_string.out + "gfhgf" + 5.out);
			print(b.out);
			from
				i = 0
			until
				i /= 5
			loop
				print(i.out);
				i++
			end
			from
				i = 0
			until
				i >= 6
			loop
				print(p.out);
				i++
			end
			t := lol();
			print(t.out);
		end

	lol : STRING
		local
			INTEGER : a
		do
			a := 1;
			print(a.out);
			result := "lol";
		end

	eiffel : INTEGER
		local
		do
			result := 0;
		end
end