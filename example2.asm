.import source "64spec.asm"

//.eval config_64spec("print_header", false)
//.eval config_64spec("write_final_results_to_file", true)
//.eval config_64spec("result_file_name", "result.txt")
.eval config_64spec("print_final_results", true)
.eval config_64spec("print_configuration", true)
.eval config_64spec("change_context_description_text_color", true)
.eval config_64spec("change_example_description_text_color", true)
.eval config_64spec("print_context_results", true)
.eval config_64spec("print_example_results", true)

sfspec: :init_spec()

 :describe("register A")
 :it("must be loaded with correct value")	
  lda #41
  :assert_a_equal #42

 :it("must be loaded with #41")	
  lda #41
  :assert_a_equal #41

 :it("must be loaded with correct value")	
  lda #01
  :assert_a_equal #60



  :finish_spec()
