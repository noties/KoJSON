

## Decide: should we limit JsonPrimitive number
Currently we have only one function for numeric types - `isNumber`, which comes
from the JSON spec, which does not separate numbers - like decimals from floats.
So, the decision must be made - should we implicitly convert to `int` when JSON
data contains real number - `"float": 4.9`? `isNumber` for that case would return
`true`, as it is a number, but when we call `json.int` should we execute an implicit
conversion from float to int? This potentially loses precision and involves some implicit
behaviour, which might be not expected. Or, maybe, it is on the contrary - we introduce
inconvenience around very simple thing.

Strictly speaking the spec itself only defines `number` type, so introducing something else might
be not required. So, let's make 